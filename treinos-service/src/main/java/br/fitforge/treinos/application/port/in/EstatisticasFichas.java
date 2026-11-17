package br.fitforge.treinos.application.port.in;

import java.math.BigDecimal;
import java.util.Map;

/** Indicadores do catálogo de fichas. */
public record EstatisticasFichas(long totalFichas, long totalExercicios, BigDecimal volumeTotalKg,
                                 long totalSessoesRealizadas, Map<String, Long> fichasPorDia,
                                 Map<String, Long> exerciciosPorGrupo) {}
