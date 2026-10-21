package br.fitforge.progresso.dto;

import java.time.LocalDate;

/** Um dia da semana atual (mesmo formato de weeklyBuckets() do frontend). */
public record DiaSemanaDTO(LocalDate date, String label, long count, long minutes) {}
