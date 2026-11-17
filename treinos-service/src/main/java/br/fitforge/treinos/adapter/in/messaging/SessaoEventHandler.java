package br.fitforge.treinos.adapter.in.messaging;

import br.fitforge.treinos.application.port.in.ProcessarEventoSessaoUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Traduz a mensagem recebida do Service Bus em chamada ao caso de uso. */
public class SessaoEventHandler {

    private static final Logger log = LoggerFactory.getLogger(SessaoEventHandler.class);

    private final ProcessarEventoSessaoUseCase useCase;
    private final ObjectMapper json;

    public SessaoEventHandler(ProcessarEventoSessaoUseCase useCase, ObjectMapper json) {
        this.useCase = useCase;
        this.json = json;
    }

    public void handle(String corpo) throws Exception {
        SessaoEvento evento = json.readValue(corpo, SessaoEvento.class);
        log.info("[SERVICE BUS] recebido {} sessaoId={} fichaId={}", evento.tipo(), evento.sessaoId(), evento.fichaId());
        if (SessaoEvento.SESSAO_REGISTRADA.equals(evento.tipo())) {
            useCase.sessaoRegistrada(evento.sessaoId(), evento.fichaId(), evento.data());
        } else if (SessaoEvento.SESSAO_EXCLUIDA.equals(evento.tipo())) {
            useCase.sessaoExcluida(evento.sessaoId(), evento.fichaId());
        } else {
            log.warn("[SERVICE BUS] tipo de evento desconhecido: {}", evento.tipo());
        }
    }
}
