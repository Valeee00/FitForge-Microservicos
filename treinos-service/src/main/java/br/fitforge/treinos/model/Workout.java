package br.fitforge.treinos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Ficha de treino (agregado raiz). Os nomes dos campos JSON são exatamente
 * os usados pelo frontend FitForge: id, name, description, days, exercises.
 */
@Entity
@Table(name = "fichas")
public class Workout {

    @Id
    @Column(length = 80)
    private String id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 250)
    private String description = "";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ficha_dias", joinColumns = @JoinColumn(name = "ficha_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "dia", length = 3)
    private List<String> days = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ficha_exercicios", joinColumns = @JoinColumn(name = "ficha_id"))
    @OrderColumn(name = "ordem")
    private List<Exercise> exercises = new ArrayList<>();

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

    @JsonIgnore
    public BigDecimal getVolume() {
        return exercises.stream().map(Exercise::volume).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getDays() { return days; }
    public void setDays(List<String> days) { this.days = days; }
    public List<Exercise> getExercises() { return exercises; }
    public void setExercises(List<Exercise> exercises) { this.exercises = exercises; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
