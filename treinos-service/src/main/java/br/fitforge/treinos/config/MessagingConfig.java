package br.fitforge.treinos.config;

import br.fitforge.treinos.adapter.in.messaging.ServiceBusSessaoListener;
import br.fitforge.treinos.adapter.in.messaging.SessaoEventHandler;
import br.fitforge.treinos.adapter.out.messaging.LogOnlyFichaEventPublisher;
import br.fitforge.treinos.adapter.out.messaging.ServiceBusFichaEventPublisher;
import br.fitforge.treinos.application.port.in.ProcessarEventoSessaoUseCase;
import br.fitforge.treinos.application.port.out.FichaEventPublisherPort;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Liga os adaptadores do Azure Service Bus (ou a versão local, sem connection string). */
@Configuration
public class MessagingConfig {

    @Value("${app.servicebus.connection-string:}")
    private String connectionString;

    @Bean
    public FichaEventPublisherPort fichaEventPublisher(ObjectMapper json,
                                                       @Value("${app.servicebus.fila-fichas:fichas-eventos}") String fila) {
        if (connectionString == null || connectionString.isBlank()) {
            return new LogOnlyFichaEventPublisher();
        }
        var sender = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(fila)
                .buildClient();
        return new ServiceBusFichaEventPublisher(sender, json);
    }

    @Bean
    public ServiceBusSessaoListener sessaoListener(ProcessarEventoSessaoUseCase useCase, ObjectMapper json,
                                                   @Value("${app.servicebus.fila-sessoes:sessoes-eventos}") String fila) {
        return new ServiceBusSessaoListener(connectionString, fila, new SessaoEventHandler(useCase, json));
    }
}
