package br.fitforge.treinos.adapter.in.web.dto;

import java.math.BigDecimal;

public record ExerciseResponse(String id, String name, String muscle, int sets, int reps, BigDecimal load, int rest) {}
