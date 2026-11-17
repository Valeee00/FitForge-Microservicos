package br.fitforge.progresso.domain.model;

import java.time.LocalDate;

/** Um dia da semana atual (mesmo formato de weeklyBuckets() do frontend). */
public record DiaSemana(LocalDate date, String label, long count, long minutes) {}
