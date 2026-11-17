package br.fitforge.treinos.adapter.out.messaging;

import br.fitforge.treinos.application.port.out.FichaEventPublisherPort;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Adaptador de saída: publica eventos de ficha no Azure Service Bus.
 * Uma falha de mensageria é registrada em log e não desfaz o CRUD.
 */
public class ServiceBusFichaEventPublisher implements FichaEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusFichaEventPublisher.class);

    private final ServiceBusSenderClient sender;
    private final ObjectMapper json;

    public ServiceBusFichaEventPublisher(ServiceBusSenderClient sender, ObjectMapper json) {
        this.sender = sender;
        this.json = json;
    }

    @Override
    public void fichaRenomeada(String fichaId, String novoNome) {
        publicar(new FichaEvento(FichaEvento.FICHA_RENOMEADA, fichaId, novoNome, OffsetDateTime.now()));
    }

    @Override
    public void fichaExcluida(String fichaId, String nome) {
        publicar(new FichaEvento(FichaEvento.FICHA_EXCLUIDA, fichaId, nome, OffsetDateTime.now()));
    }

    private void publicar(FichaEvento evento) {
        try {
            ServiceBusMessage msg = new ServiceBusMessage(json.writeValueAsString(evento))
                    .setContentType("application/json")
                    .setSubject(evento.tipo())
                    .setMessageId(UUID.randomUUID().toString());
            sender.sendMessage(msg);
            log.info("[SERVICE BUS] publicado {} fichaId={}", evento.tipo(), evento.fichaId());
        } catch (Exception e) {
            log.error("[SERVICE BUS] falha ao publicar {}: {}", evento.tipo(), e.toString());
        }
    }
}
