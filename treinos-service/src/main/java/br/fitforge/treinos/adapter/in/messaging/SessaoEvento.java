package br.fitforge.treinos.adapter.in.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

/** Mensagem consumida da fila "sessoes-eventos" (publicada pelo progresso-service). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SessaoEvento(String tipo, String sessaoId, String fichaId, LocalDate data) {
    public static final String SESSAO_REGISTRADA = "SESSAO_REGISTRADA";
    public static final String SESSAO_EXCLUIDA = "SESSAO_EXCLUIDA";
}
