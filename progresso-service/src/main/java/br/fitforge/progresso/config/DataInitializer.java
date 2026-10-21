package br.fitforge.progresso.config;

import br.fitforge.progresso.model.Session;
import br.fitforge.progresso.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Sessões de demonstração (relativas à data de hoje) ligadas às fichas de exemplo
 * do treinos-service. Desligue com SEED_DATA=false.
 */
@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final SessionRepository repository;
    private final Clock clock;

    public DataInitializer(SessionRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;
        LocalDate hoje = LocalDate.now(clock);
        repository.save(sessao("demo-1", "template-a", "Treino A · Peito e tríceps", hoje.minusDays(6), 55, 7, "78.5", "Supino evoluiu 2 kg."));
        repository.save(sessao("demo-2", "template-b", "Treino B · Costas e bíceps", hoje.minusDays(4), 50, 8, null, ""));
        repository.save(sessao("demo-3", "template-c", "Treino C · Pernas e ombros", hoje.minusDays(2), 65, 9, "78.2", "Leg press pesado hoje."));
        repository.save(sessao("demo-4", "template-a", "Treino A · Peito e tríceps", hoje.minusDays(1), 45, 6, null, ""));
        log.info("[SEED] 4 sessões de demonstração carregadas.");
    }

    private static Session sessao(String id, String fichaId, String ficha, LocalDate data, int min, int esforco,
                                  String peso, String obs) {
        Session s = new Session();
        s.setId(id);
        s.setWorkoutId(fichaId);
        s.setWorkoutName(ficha);
        s.setDate(data);
        s.setDuration(min);
        s.setEffort(esforco);
        s.setBodyWeight(peso == null ? null : new BigDecimal(peso));
        s.setNotes(obs);
        return s;
    }
}
