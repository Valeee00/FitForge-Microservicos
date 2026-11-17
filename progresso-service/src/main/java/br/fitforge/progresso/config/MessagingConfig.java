package br.fitforge.progresso.config;

import br.fitforge.progresso.adapter.in.messaging.FichaEventHandler;
import br.fitforge.progresso.adapter.in.messaging.ServiceBusFichaListener;
import br.fitforge.progresso.adapter.out.messaging.LogOnlySessaoEventPublisher;
import br.fitforge.progresso.adapter.out.messaging.ServiceBusSessaoEventPublisher;
import br.fitforge.progresso.application.port.in.ProcessarEventoFichaUseCase;
import br.fitforge.progresso.application.port.out.SessaoEventPublisherPort;
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
    public SessaoEventPublisherPort sessaoEventPublisher(ObjectMapper json,
                                                         @Value("${app.servicebus.fila-sessoes:sessoes-eventos}") String fila) {
        if (connectionString == null || connectionString.isBlank()) {
            return new LogOnlySessaoEventPublisher();
        }
        var sender = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(fila)
                .buildClient();
        return new ServiceBusSessaoEventPublisher(sender, json);
    }

    @Bean
    public ServiceBusFichaListener fichaListener(ProcessarEventoFichaUseCase useCase, ObjectMapper json,
                                                 @Value("${app.servicebus.fila-fichas:fichas-eventos}") String fila) {
        return new ServiceBusFichaListener(connectionString, fila, new FichaEventHandler(useCase, json));
    }
}
