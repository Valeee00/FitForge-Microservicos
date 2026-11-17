package br.fitforge.progresso.adapter.out.messaging;

import br.fitforge.progresso.application.port.out.SessaoEventPublisherPort;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Adaptador de saída: publica eventos de sessão no Azure Service Bus (falha só gera log). */
public class ServiceBusSessaoEventPublisher implements SessaoEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusSessaoEventPublisher.class);

    private final ServiceBusSenderClient sender;
    private final ObjectMapper json;

    public ServiceBusSessaoEventPublisher(ServiceBusSenderClient sender, ObjectMapper json) {
        this.sender = sender;
        this.json = json;
    }

    @Override
    public void sessaoRegistrada(String sessaoId, String fichaId, LocalDate data) {
        publicar(new SessaoEvento(SessaoEvento.SESSAO_REGISTRADA, sessaoId, fichaId, data, OffsetDateTime.now()));
    }

    @Override
    public void sessaoExcluida(String sessaoId, String fichaId) {
        publicar(new SessaoEvento(SessaoEvento.SESSAO_EXCLUIDA, sessaoId, fichaId, null, OffsetDateTime.now()));
    }

    private void publicar(SessaoEvento evento) {
        try {
            ServiceBusMessage msg = new ServiceBusMessage(json.writeValueAsString(evento))
                    .setContentType("application/json")
                    .setSubject(evento.tipo())
                    .setMessageId(UUID.randomUUID().toString());
            sender.sendMessage(msg);
            log.info("[SERVICE BUS] publicado {} sessaoId={} fichaId={}", evento.tipo(), evento.sessaoId(), evento.fichaId());
        } catch (Exception e) {
            log.error("[SERVICE BUS] falha ao publicar {}: {}", evento.tipo(), e.toString());
        }
    }
}
