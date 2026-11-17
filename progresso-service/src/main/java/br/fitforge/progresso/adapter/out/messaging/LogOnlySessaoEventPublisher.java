package br.fitforge.progresso.adapter.out.messaging;

import br.fitforge.progresso.application.port.out.SessaoEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

/** Usado quando SERVICEBUS_CONNECTION_STRING não está configurada (execução local e testes). */
public class LogOnlySessaoEventPublisher implements SessaoEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(LogOnlySessaoEventPublisher.class);

    @Override
    public void sessaoRegistrada(String sessaoId, String fichaId, LocalDate data) {
        log.info("[EVENTO local] SESSAO_REGISTRADA sessaoId={} fichaId={} data={}", sessaoId, fichaId, data);
    }

    @Override
    public void sessaoExcluida(String sessaoId, String fichaId) {
        log.info("[EVENTO local] SESSAO_EXCLUIDA sessaoId={} fichaId={}", sessaoId, fichaId);
    }
}
