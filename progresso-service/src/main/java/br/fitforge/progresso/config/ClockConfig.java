package br.fitforge.progresso.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * "Hoje" é sempre calculado no fuso do usuário (padrão America/Sao_Paulo),
 * mesmo com o servidor do Azure rodando em UTC.
 */
@Configuration
public class ClockConfig {
    @Bean
    public Clock clock(@Value("${app.timezone:America/Sao_Paulo}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
