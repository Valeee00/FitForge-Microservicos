package br.fitforge.treinos.adapter.in.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Ficha devolvida pela API. Os campos são exatamente os do frontend FitForge
 * (id, name, description, days, exercises) + os indicadores atualizados por mensageria.
 */
public record WorkoutResponse(String id, String name, String description, List<String> days,
                              List<ExerciseResponse> exercises, int totalSessoes, LocalDate ultimaSessao,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {}
