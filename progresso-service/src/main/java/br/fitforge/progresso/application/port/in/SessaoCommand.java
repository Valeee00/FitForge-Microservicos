package br.fitforge.progresso.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Dados de entrada para registrar/alterar uma sessão (independente de HTTP). */
public record SessaoCommand(String id, String workoutId, String workoutName, LocalDate date, int duration,
                            int effort, BigDecimal bodyWeight, String notes) {}
