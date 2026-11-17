package br.fitforge.progresso.adapter.in.messaging;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

/**
 * Adaptador de entrada assíncrono: consome a fila "fichas-eventos" do Azure Service Bus.
 * Sem connection string configurada, fica desligado (execução local e testes).
 */
public class ServiceBusFichaListener implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusFichaListener.class);

    private final String connectionString;
    private final String fila;
    private final FichaEventHandler handler;
    private ServiceBusProcessorClient processor;
    private volatile boolean rodando;

    public ServiceBusFichaListener(String connectionString, String fila, FichaEventHandler handler) {
        this.connectionString = connectionString;
        this.fila = fila;
        this.handler = handler;
    }

    @Override
    public void start() {
        if (connectionString == null || connectionString.isBlank()) {
            log.warn("[SERVICE BUS] SERVICEBUS_CONNECTION_STRING não definida: consumidor de '{}' desligado.", fila);
            rodando = true;
            return;
        }
        try {
            processor = new ServiceBusClientBuilder()
                    .connectionString(connectionString)
                    .processor()
                    .queueName(fila)
                    .processMessage(ctx -> {
                        try {
                            handler.handle(ctx.getMessage().getBody().toString());
                        } catch (Exception e) {
                            throw new IllegalStateException("Falha ao processar mensagem", e);
                        }
                    })
                    .processError(err -> log.warn("[SERVICE BUS] erro no consumidor: {}", err.getException().toString()))
                    .buildProcessorClient();
            processor.start();
            log.info("[SERVICE BUS] consumindo a fila '{}'", fila);
        } catch (Exception e) {
            log.error("[SERVICE BUS] não foi possível iniciar o consumidor: {}", e.toString());
        }
        rodando = true;
    }

    @Override
    public void stop() {
        if (processor != null) processor.close();
        rodando = false;
    }

    @Override
    public boolean isRunning() {
        return rodando;
    }
}
