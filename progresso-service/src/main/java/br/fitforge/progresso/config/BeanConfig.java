package br.fitforge.progresso.config;

import br.fitforge.progresso.application.port.out.SessaoEventPublisherPort;
import br.fitforge.progresso.application.port.out.SessaoRepositoryPort;
import br.fitforge.progresso.application.service.SessaoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/** Composição da aplicação (Hexagonal): liga o núcleo (SessaoService) aos adaptadores. */
@Configuration
public class BeanConfig {

    /** "Hoje" no fuso do usuário (padrão America/Sao_Paulo), mesmo com o servidor em UTC. */
    @Bean
    public Clock clock(@Value("${app.timezone:America/Sao_Paulo}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }

    @Bean
    public SessaoService sessaoService(SessaoRepositoryPort repositorio, SessaoEventPublisherPort eventos, Clock clock) {
        return new SessaoService(repositorio, eventos, clock);
    }
}
