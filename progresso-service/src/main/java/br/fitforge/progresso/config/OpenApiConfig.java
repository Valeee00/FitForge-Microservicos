package br.fitforge.progresso.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("FitForge — progresso-service")
                .version("1.0.0")
                .description("Microsserviço de progresso (CRUD de sessões de treino e indicadores)."));
    }
}
