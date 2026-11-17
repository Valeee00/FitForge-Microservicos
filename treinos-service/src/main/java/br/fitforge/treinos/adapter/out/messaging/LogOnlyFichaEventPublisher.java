package br.fitforge.treinos.adapter.out.messaging;

import br.fitforge.treinos.application.port.out.FichaEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Usado quando SERVICEBUS_CONNECTION_STRING não está configurada (execução local e testes). */
public class LogOnlyFichaEventPublisher implements FichaEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(LogOnlyFichaEventPublisher.class);

    @Override
    public void fichaRenomeada(String fichaId, String novoNome) {
        log.info("[EVENTO local] FICHA_RENOMEADA fichaId={} nome={}", fichaId, novoNome);
    }

    @Override
    public void fichaExcluida(String fichaId, String nome) {
        log.info("[EVENTO local] FICHA_EXCLUIDA fichaId={} nome={}", fichaId, nome);
    }
}
