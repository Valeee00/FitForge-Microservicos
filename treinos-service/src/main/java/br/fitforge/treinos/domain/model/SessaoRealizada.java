package br.fitforge.treinos.domain.model;

import java.time.LocalDate;

/**
 * Referência a uma sessão registrada no progresso-service, recebida por mensagem
 * (Azure Service Bus). Guardar o id torna o processamento idempotente.
 */
public record SessaoRealizada(String sessaoId, LocalDate data) {}
