package br.fitforge.treinos.config;

import br.fitforge.treinos.application.port.out.FichaRepositoryPort;
import br.fitforge.treinos.domain.model.Exercise;
import br.fitforge.treinos.domain.model.Workout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Carrega as 3 fichas de exemplo (mesmos ids do frontend) quando o banco está vazio.
 * Se o MongoDB estiver inacessível, só registra o erro: o app sobe mesmo assim
 * (evita reinícios em loop no Azure, que consomem a cota do plano gratuito).
 */
@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final FichaRepositoryPort repositorio;
    private final Clock clock;

    public DataInitializer(FichaRepositoryPort repositorio, Clock clock) {
        this.repositorio = repositorio;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        try {
            if (repositorio.contar() > 0) return;
            salvar(new Workout("template-a", "Treino A · Peito e tríceps",
                    "Ficha de exemplo. Ajuste os exercícios e cargas ao seu plano.", List.of("Seg", "Qui"), List.of(
                    e("a1", "Supino reto", "Peito", 3, 12, 20, 90),
                    e("a2", "Supino inclinado com halteres", "Peito", 3, 12, 10, 60),
                    e("a3", "Tríceps na polia", "Braços", 3, 12, 15, 60),
                    e("a4", "Abdominal no solo", "Abdômen", 3, 15, 0, 60))));
            salvar(new Workout("template-b", "Treino B · Costas e bíceps",
                    "Ficha de exemplo. Ajuste ao seu plano de treino.", List.of("Ter", "Sex"), List.of(
                    e("b1", "Puxada frontal", "Costas", 3, 12, 25, 90),
                    e("b2", "Remada baixa", "Costas", 3, 12, 20, 60),
                    e("b3", "Rosca com halteres", "Braços", 3, 12, 8, 60))));
            salvar(new Workout("template-c", "Treino C · Pernas e ombros",
                    "Ficha de exemplo. Ajuste ao seu plano de treino.", List.of("Qua"), List.of(
                    e("c1", "Agachamento livre", "Pernas", 3, 10, 20, 90),
                    e("c2", "Leg press", "Pernas", 3, 12, 40, 90),
                    e("c3", "Elevação lateral", "Ombros", 3, 12, 5, 60))));
            log.info("[SEED] 3 fichas de exemplo carregadas no MongoDB.");
        } catch (Exception ex) {
            log.error("[SEED] MongoDB indisponível ({}). Verifique MONGODB_URI.", ex.toString());
        }
    }

    private void salvar(Workout w) {
        w.marcarPersistencia(LocalDateTime.now(clock));
        repositorio.salvar(w);
    }

    private static Exercise e(String id, String nome, String grupo, int series, int reps, int carga, int descanso) {
        return new Exercise(id, nome, grupo, series, reps, BigDecimal.valueOf(carga), descanso);
    }
}
