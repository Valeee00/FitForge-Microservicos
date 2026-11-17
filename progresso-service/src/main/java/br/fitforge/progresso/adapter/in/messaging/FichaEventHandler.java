package br.fitforge.progresso.adapter.in.messaging;

import br.fitforge.progresso.application.port.in.ProcessarEventoFichaUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Traduz a mensagem recebida do Service Bus em chamada ao caso de uso. */
public class FichaEventHandler {

    private static final Logger log = LoggerFactory.getLogger(FichaEventHandler.class);

    private final ProcessarEventoFichaUseCase useCase;
    private final ObjectMapper json;

    public FichaEventHandler(ProcessarEventoFichaUseCase useCase, ObjectMapper json) {
        this.useCase = useCase;
        this.json = json;
    }

    public void handle(String corpo) throws Exception {
        FichaEvento evento = json.readValue(corpo, FichaEvento.class);
        log.info("[SERVICE BUS] recebido {} fichaId={}", evento.tipo(), evento.fichaId());
        if (FichaEvento.FICHA_RENOMEADA.equals(evento.tipo())) {
            useCase.fichaRenomeada(evento.fichaId(), evento.nome());
        } else if (FichaEvento.FICHA_EXCLUIDA.equals(evento.tipo())) {
            useCase.fichaExcluida(evento.fichaId());
        } else {
            log.warn("[SERVICE BUS] tipo de evento desconhecido: {}", evento.tipo());
        }
    }
}
