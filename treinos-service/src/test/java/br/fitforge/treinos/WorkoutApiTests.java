package br.fitforge.treinos;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
class WorkoutApiTests {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void configurarMongo(DynamicPropertyRegistry registry) {
        String banco = "fitforge_teste_" + UUID.randomUUID().toString().replace("-", "");
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> banco);
        registry.add("app.seed", () -> true);
    }

    @Autowired
    MockMvc mvc;

    private static final String FICHA = """
            {"id":"ficha-teste","name":"Treino D · Full body","description":"teste",
             "days":["Sex","Seg"],
             "exercises":[{"id":"d1","name":"Burpee","muscle":"Corpo inteiro","sets":3,"reps":15,"load":0,"rest":45},
                          {"id":"d2","name":"Terra","muscle":"Costas","sets":4,"reps":8,"load":60,"rest":120}]}
            """;

    @Test
    void listaAsFichasDeExemplo() throws Exception {
        mvc.perform(get("/api/treinos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].id", hasItem("template-a")));
    }

    @Test
    void crudCompleto() throws Exception {
        mvc.perform(post("/api/treinos").contentType(MediaType.APPLICATION_JSON).content(FICHA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/treinos/ficha-teste")))
                .andExpect(jsonPath("$.days", contains("Seg", "Sex")))       // ordenados Seg..Dom
                .andExpect(jsonPath("$.exercises[1].load").value(60));

        mvc.perform(get("/api/treinos/ficha-teste")).andExpect(status().isOk())
                .andExpect(jsonPath("$.exercises", hasSize(2)));

        mvc.perform(put("/api/treinos/ficha-teste").contentType(MediaType.APPLICATION_JSON)
                        .content(FICHA.replace("Treino D · Full body", "Treino D editado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Treino D editado"));

        mvc.perform(get("/api/treinos").param("dia", "Sex")).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/treinos").param("busca", "editado")).andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(delete("/api/treinos/ficha-teste")).andExpect(status().isNoContent());
        mvc.perform(get("/api/treinos/ficha-teste")).andExpect(status().isNotFound());
    }

    @Test
    void idDuplicadoRetorna409() throws Exception {
        mvc.perform(post("/api/treinos").contentType(MediaType.APPLICATION_JSON)
                        .content(FICHA.replace("ficha-teste", "template-a")))
                .andExpect(status().isConflict());
    }

    @Test
    void validaCamposComoOFrontend() throws Exception {
        String invalida = """
                {"name":"X","days":[],"exercises":[{"name":"Supino","muscle":"Pescoço","sets":0,"reps":12,"load":-1,"rest":60}]}
                """;
        mvc.perform(post("/api/treinos").contentType(MediaType.APPLICATION_JSON).content(invalida))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.days").exists());
    }

    @Test
    void estatisticas() throws Exception {
        mvc.perform(get("/api/treinos/estatisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFichas").value(3))
                .andExpect(jsonPath("$.totalExercicios").value(10))
                .andExpect(jsonPath("$.fichasPorDia.Seg").value(1));
    }

    @Test
    void rotaInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/naoexiste")).andExpect(status().isNotFound());
    }
}
