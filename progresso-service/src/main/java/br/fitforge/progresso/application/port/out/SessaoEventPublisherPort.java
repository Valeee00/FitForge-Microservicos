package br.fitforge.progresso.application.port.out;

import java.time.LocalDate;

/** Porta de saída: publicação de eventos de sessão (implementada pelo adaptador Azure Service Bus). */
public interface SessaoEventPublisherPort {
    void sessaoRegistrada(String sessaoId, String fichaId, LocalDate data);

    void sessaoExcluida(String sessaoId, String fichaId);
}
