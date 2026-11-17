package br.fitforge.progresso.application.port.in;

import br.fitforge.progresso.domain.model.DiaSemana;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Indicadores de progresso. */
public record EstatisticasProgresso(long totalSessoes, long minutosTotais, long sessoesNaSemana, long minutosNaSemana,
                                    long sessoesNoMes, Double esforcoMedio, BigDecimal ultimoPesoKg,
                                    LocalDate ultimaSessao, int sequenciaDiasSeguidos, List<DiaSemana> semana,
                                    Map<String, Long> sessoesPorFicha) {}
