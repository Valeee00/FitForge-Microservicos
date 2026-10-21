package br.fitforge.progresso.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sessão de treino realizada. Campos JSON idênticos ao frontend FitForge:
 * id, workoutId, workoutName, date (AAAA-MM-DD), duration, effort, bodyWeight, notes.
 */
@Entity
@Table(name = "sessoes", indexes = {
        @Index(name = "idx_sessao_data", columnList = "data_sessao"),
        @Index(name = "idx_sessao_ficha", columnList = "workout_id")
})
public class Session {

    @Id
    @Column(length = 80)
    private String id;

    @Column(name = "workout_id", nullable = false, length = 80)
    private String workoutId;

    @Column(nullable = false, length = 80)
    private String workoutName;

    @Column(name = "data_sessao", nullable = false)
    private LocalDate date;

    /** Duração em minutos (1 a 360). */
    private int duration;

    /** Esforço percebido (1 a 10). */
    private int effort;

    /** Peso corporal em kg (opcional). */
    @Column(precision = 5, scale = 1)
    private BigDecimal bodyWeight;

    @Column(nullable = false, length = 500)
    private String notes = "";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void aoCriar() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void aoAtualizar() {
        updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getWorkoutId() { return workoutId; }
    public void setWorkoutId(String workoutId) { this.workoutId = workoutId; }
    public String getWorkoutName() { return workoutName; }
    public void setWorkoutName(String workoutName) { this.workoutName = workoutName; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public int getEffort() { return effort; }
    public void setEffort(int effort) { this.effort = effort; }
    public BigDecimal getBodyWeight() { return bodyWeight; }
    public void setBodyWeight(BigDecimal bodyWeight) { this.bodyWeight = bodyWeight; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
