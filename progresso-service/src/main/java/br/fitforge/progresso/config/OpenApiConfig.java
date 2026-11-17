package br.fitforge.progresso.config;

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
                .title("FitForge — progresso-service")
                .version("2.0.0")
                .description("Microsserviço 2 (Azure SQL): CRUD de sessões de treino. Arquitetura Hexagonal. "
                        + "Publica SESSAO_REGISTRADA/SESSAO_EXCLUIDA e consome FICHA_RENOMEADA/FICHA_EXCLUIDA no Azure Service Bus."));
        if (serverUrl != null && !serverUrl.isBlank()) {
            api.addServersItem(new Server().url(serverUrl).description("Via API Gateway"));
        }
        return api;
    }
}
