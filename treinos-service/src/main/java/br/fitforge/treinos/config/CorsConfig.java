package br.fitforge.treinos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS para acesso DIRETO do frontend (fase sem gateway / testes locais).
 * Depois que o API Gateway entra, o CORS passa a ser centralizado nele.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.origins}")
    private String[] origens;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origens)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Correlation-Id")
                .maxAge(3600);
    }
}
