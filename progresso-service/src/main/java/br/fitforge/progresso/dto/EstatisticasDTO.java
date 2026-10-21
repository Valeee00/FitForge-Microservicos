package br.fitforge.progresso.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Indicadores de progresso (GET /api/sessoes/estatisticas). */
public record EstatisticasDTO(
        long totalSessoes,
        long minutosTotais,
        long sessoesNaSemana,
        long minutosNaSemana,
        long sessoesNoMes,
        Double esforcoMedio,
        BigDecimal ultimoPesoKg,
        LocalDate ultimaSessao,
        int sequenciaDiasSeguidos,
        List<DiaSemanaDTO> semana,
        Map<String, Long> sessoesPorFicha
) {}
