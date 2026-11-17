package br.fitforge.progresso.adapter.in.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Mensagem consumida da fila "fichas-eventos" (publicada pelo treinos-service). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FichaEvento(String tipo, String fichaId, String nome) {
    public static final String FICHA_RENOMEADA = "FICHA_RENOMEADA";
    public static final String FICHA_EXCLUIDA = "FICHA_EXCLUIDA";
}
