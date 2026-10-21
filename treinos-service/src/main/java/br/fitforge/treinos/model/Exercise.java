package br.fitforge.treinos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

/** Exercício de uma ficha. Value object embutido na ficha (não existe sozinho). */
@Embeddable
public class Exercise {

    @Column(name = "exercise_id", length = 80, nullable = false)
    private String id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 20)
    private String muscle;

    private int sets;
    private int reps;

    /** Carga em kg (0 a 500). */
    @Column(precision = 6, scale = 2)
    private BigDecimal load;

    /** Descanso em segundos. */
    @Column(name = "rest_seconds")
    private int rest;

    public Exercise() {
    }

    public Exercise(String id, String name, String muscle, int sets, int reps, BigDecimal load, int rest) {
        this.id = id;
        this.name = name;
        this.muscle = muscle;
        this.sets = sets;
        this.reps = reps;
        this.load = load;
        this.rest = rest;
    }

    /** Volume = séries × repetições × carga. */
    public BigDecimal volume() {
        return load == null ? BigDecimal.ZERO : load.multiply(BigDecimal.valueOf((long) sets * reps));
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMuscle() { return muscle; }
    public void setMuscle(String muscle) { this.muscle = muscle; }
    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }
    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }
    public BigDecimal getLoad() { return load; }
    public void setLoad(BigDecimal load) { this.load = load; }
    public int getRest() { return rest; }
    public void setRest(int rest) { this.rest = rest; }
}
