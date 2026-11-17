package br.fitforge.treinos.adapter.out.persistence;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Documento MongoDB da coleção "treinos". Só existe no adaptador de persistência. */
@Document(collection = "treinos")
public class WorkoutDocument {

    @MongoId(FieldType.STRING)
    private String id;
    private String name;
    private String description;
    private List<String> days = new ArrayList<>();
    private List<ExerciseDocument> exercises = new ArrayList<>();
    private List<SessaoDocument> sessoes = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static class ExerciseDocument {
        private String id;
        private String name;
        private String muscle;
        private int sets;
        private int reps;
        @Field(targetType = FieldType.DECIMAL128)
        private BigDecimal load;
        private int rest;

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

    public static class SessaoDocument {
        private String sessaoId;
        private LocalDate data;

        public String getSessaoId() { return sessaoId; }
        public void setSessaoId(String sessaoId) { this.sessaoId = sessaoId; }
        public LocalDate getData() { return data; }
        public void setData(LocalDate data) { this.data = data; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getDays() { return days; }
    public void setDays(List<String> days) { this.days = days; }
    public List<ExerciseDocument> getExercises() { return exercises; }
    public void setExercises(List<ExerciseDocument> exercises) { this.exercises = exercises; }
    public List<SessaoDocument> getSessoes() { return sessoes; }
    public void setSessoes(List<SessaoDocument> sessoes) { this.sessoes = sessoes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
