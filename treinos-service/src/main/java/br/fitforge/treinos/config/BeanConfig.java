package br.fitforge.treinos.config;

import br.fitforge.treinos.application.port.out.FichaEventPublisherPort;
import br.fitforge.treinos.application.port.out.FichaRepositoryPort;
import br.fitforge.treinos.application.service.FichaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Composição da aplicação (Hexagonal): liga o núcleo (FichaService) aos adaptadores.
 * O núcleo não tem anotações do Spring — quem conhece o framework é esta classe.
 */
@Configuration
public class BeanConfig {

    @Bean
    public Clock clock(@Value("${app.timezone:America/Sao_Paulo}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }

    @Bean
    public FichaService fichaService(FichaRepositoryPort repositorio, FichaEventPublisherPort eventos, Clock clock) {
        return new FichaService(repositorio, eventos, clock);
    }
}
