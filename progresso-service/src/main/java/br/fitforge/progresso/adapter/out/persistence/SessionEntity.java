package br.fitforge.progresso.adapter.out.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Tabela "sessoes" no Azure SQL Database. Só existe no adaptador de persistência. */
@Entity
@Table(name = "sessoes", indexes = {
        @Index(name = "idx_sessao_data", columnList = "data_sessao"),
        @Index(name = "idx_sessao_ficha", columnList = "workout_id")
})
public class SessionEntity {

    @Id
    @Column(length = 80)
    private String id;

    @Column(name = "workout_id", nullable = false, length = 80)
    private String workoutId;

    @Column(name = "workout_name", nullable = false, length = 80)
    private String workoutName;

    @Column(name = "data_sessao", nullable = false)
    private LocalDate date;

    @Column(name = "duracao_min", nullable = false)
    private int duration;

    @Column(name = "esforco", nullable = false)
    private int effort;

    @Column(name = "peso_kg", precision = 5, scale = 1)
    private BigDecimal bodyWeight;

    @Column(name = "observacoes", nullable = false, length = 500)
    private String notes = "";

    @Column(name = "ficha_ativa", nullable = false)
    private boolean fichaAtiva = true;

    @Column(name = "criado_em")
    private LocalDateTime createdAt;

    @Column(name = "atualizado_em")
    private LocalDateTime updatedAt;

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
    public boolean isFichaAtiva() { return fichaAtiva; }
    public void setFichaAtiva(boolean fichaAtiva) { this.fichaAtiva = fichaAtiva; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
