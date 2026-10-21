package br.fitforge.treinos.dto;

import java.math.BigDecimal;
import java.util.Map;

/** Indicadores do catálogo de fichas (GET /api/treinos/estatisticas). */
public record EstatisticasDTO(
        long totalFichas,
        long totalExercicios,
        BigDecimal volumeTotalKg,
        Map<String, Long> fichasPorDia,
        Map<String, Long> exerciciosPorGrupo
) {}
