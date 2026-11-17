package br.fitforge.treinos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openApi(@Value("${app.openapi.server-url:}") String serverUrl) {
        OpenAPI api = new OpenAPI().info(new Info()
                .title("FitForge — treinos-service")
                .version("2.0.0")
                .description("Microsserviço 1 (MongoDB): CRUD de fichas de treino. Arquitetura Hexagonal. "
                        + "Publica FICHA_RENOMEADA/FICHA_EXCLUIDA e consome SESSAO_REGISTRADA/SESSAO_EXCLUIDA no Azure Service Bus."));
        if (serverUrl != null && !serverUrl.isBlank()) {
            api.addServersItem(new Server().url(serverUrl).description("Via API Gateway"));
        }
        return api;
    }
}
