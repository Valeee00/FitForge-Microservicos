package br.fitforge.progresso.config;

import br.fitforge.progresso.application.port.out.SessaoRepositoryPort;
import br.fitforge.progresso.domain.model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sessões de demonstração (relativas a hoje) ligadas às fichas de exemplo.
 * Desligue com SEED_DATA=false. Se o banco estiver fora, só registra o erro.
 */
@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final SessaoRepositoryPort repositorio;
    private final Clock clock;

    public DataInitializer(SessaoRepositoryPort repositorio, Clock clock) {
        this.repositorio = repositorio;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        try {
            if (repositorio.contar() > 0) return;
            LocalDate hoje = LocalDate.now(clock);
            salvar("demo-1", "template-a", "Treino A · Peito e tríceps", hoje.minusDays(6), 55, 7, "78.5", "Supino evoluiu 2 kg.");
            salvar("demo-2", "template-b", "Treino B · Costas e bíceps", hoje.minusDays(4), 50, 8, null, "");
            salvar("demo-3", "template-c", "Treino C · Pernas e ombros", hoje.minusDays(2), 65, 9, "78.2", "Leg press pesado hoje.");
            salvar("demo-4", "template-a", "Treino A · Peito e tríceps", hoje.minusDays(1), 45, 6, null, "");
            log.info("[SEED] 4 sessões de demonstração carregadas.");
        } catch (Exception ex) {
            log.error("[SEED] Banco indisponível ({}). Verifique DB_URL/DB_USERNAME/DB_PASSWORD.", ex.toString());
        }
    }

    private void salvar(String id, String fichaId, String ficha, LocalDate data, int min, int esforco, String peso, String obs) {
        Session s = new Session(id, fichaId, ficha, data, min, esforco, peso == null ? null : new BigDecimal(peso), obs,
                LocalDate.now(clock));
        s.marcarPersistencia(LocalDateTime.now(clock));
        repositorio.salvar(s);
    }
}
