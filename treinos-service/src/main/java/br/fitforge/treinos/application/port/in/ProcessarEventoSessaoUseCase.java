package br.fitforge.treinos.application.port.in;

import java.time.LocalDate;

/** Porta de entrada assíncrona: eventos de sessão publicados pelo progresso-service. */
public interface ProcessarEventoSessaoUseCase {
    void sessaoRegistrada(String sessaoId, String fichaId, LocalDate data);

    void sessaoExcluida(String sessaoId, String fichaId);
}
