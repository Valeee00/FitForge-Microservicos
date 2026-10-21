package br.fitforge.progresso;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SessionApiTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    Clock clock;

    private String sessao(String id, LocalDate data) {
        return """
                {"id":"%s","workoutId":"template-b","workoutName":"Treino B · Costas e bíceps",
                 "date":"%s","duration":40,"effort":7,"bodyWeight":80.5,"notes":"ok"}
                """.formatted(id, data);
    }

    @Test
    void listaAsSessoesDeDemonstracaoMaisRecentesPrimeiro() throws Exception {
        LocalDate hoje = LocalDate.now(clock);
        mvc.perform(get("/api/sessoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].date").value(hoje.minusDays(1).toString()));
    }

    @Test
    void crudCompleto() throws Exception {
        LocalDate hoje = LocalDate.now(clock);
        mvc.perform(post("/api/sessoes").contentType(MediaType.APPLICATION_JSON).content(sessao("s-teste", hoje)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bodyWeight").value(80.5))
                .andExpect(jsonPath("$.date").value(hoje.toString()));

        mvc.perform(put("/api/sessoes/s-teste").contentType(MediaType.APPLICATION_JSON)
                        .content(sessao("s-teste", hoje).replace("\"duration\":40", "\"duration\":75")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duration").value(75));

        mvc.perform(get("/api/sessoes").param("workoutId", "template-b"))
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(delete("/api/sessoes/s-teste")).andExpect(status().isNoContent());
        mvc.perform(get("/api/sessoes/s-teste")).andExpect(status().isNotFound());
    }

    @Test
    void naoAceitaDataNoFuturo() throws Exception {
        mvc.perform(post("/api/sessoes").contentType(MediaType.APPLICATION_JSON)
                        .content(sessao("s-futuro", LocalDate.now(clock).plusDays(1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validaCampos() throws Exception {
        String invalida = """
                {"workoutId":"","workoutName":"A","date":"2026-01-01","duration":0,"effort":11,"bodyWeight":5}
                """;
        mvc.perform(post("/api/sessoes").contentType(MediaType.APPLICATION_JSON).content(invalida))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.effort").exists())
                .andExpect(jsonPath("$.fieldErrors.duration").exists());
    }

    @Test
    void estatisticasDaSemana() throws Exception {
        mvc.perform(get("/api/sessoes/estatisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSessoes").value(4))
                .andExpect(jsonPath("$.minutosTotais").value(215))
                .andExpect(jsonPath("$.semana", hasSize(7)))
                .andExpect(jsonPath("$.semana[0].label").value("Seg"))
                .andExpect(jsonPath("$.ultimoPesoKg").value(78.2));
    }
}
