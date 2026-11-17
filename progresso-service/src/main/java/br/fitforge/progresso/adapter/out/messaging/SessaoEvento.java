package br.fitforge.progresso.adapter.out.messaging;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Mensagem publicada na fila "sessoes-eventos" (contrato com o treinos-service). */
public record SessaoEvento(String tipo, String sessaoId, String fichaId, LocalDate data, OffsetDateTime ocorridoEm) {
    public static final String SESSAO_REGISTRADA = "SESSAO_REGISTRADA";
    public static final String SESSAO_EXCLUIDA = "SESSAO_EXCLUIDA";
}
