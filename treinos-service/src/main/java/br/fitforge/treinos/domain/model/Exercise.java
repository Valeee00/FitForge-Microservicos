package br.fitforge.treinos.domain.model;

import java.math.BigDecimal;

/** Exercício de uma ficha (value object). Domínio puro: sem Spring, sem banco. */
public record Exercise(String id, String name, String muscle, int sets, int reps, BigDecimal load, int rest) {

    /** Volume = séries × repetições × carga. */
    public BigDecimal volume() {
        return load == null ? BigDecimal.ZERO : load.multiply(BigDecimal.valueOf((long) sets * reps));
    }
}
