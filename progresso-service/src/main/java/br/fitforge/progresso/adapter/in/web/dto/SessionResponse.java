package br.fitforge.progresso.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Sessão devolvida pela API (campos do frontend FitForge + fichaAtiva, atualizado por mensageria). */
public record SessionResponse(String id, String workoutId, String workoutName, LocalDate date, int duration,
                              int effort, BigDecimal bodyWeight, String notes, boolean fichaAtiva,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {}
