package br.fitforge.treinos.adapter.out.messaging;

import java.time.OffsetDateTime;

/** Mensagem publicada na fila "fichas-eventos" (contrato com o progresso-service). */
public record FichaEvento(String tipo, String fichaId, String nome, OffsetDateTime ocorridoEm) {
    public static final String FICHA_RENOMEADA = "FICHA_RENOMEADA";
    public static final String FICHA_EXCLUIDA = "FICHA_EXCLUIDA";
}
