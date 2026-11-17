package br.fitforge.treinos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Ficha de treino — agregado raiz do Bounded Context "Planejamento".
 * Contém as regras de negócio; não conhece HTTP, MongoDB nem Service Bus.
 */
public class Workout {

    private final String id;
    private String name;
    private String description;
    private final List<String> days = new ArrayList<>();
    private final List<Exercise> exercises = new ArrayList<>();
    private final List<SessaoRealizada> sessoes = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Workout(String id, String name, String description, List<String> days, List<Exercise> exercises) {
        this.id = Objects.requireNonNull(id, "id");
        alterar(name, description, days, exercises);
    }

    /** Usado pelo adaptador de persistência para reconstruir o agregado. */
    public static Workout reconstituir(String id, String name, String description, List<String> days,
                                       List<Exercise> exercises, List<SessaoRealizada> sessoes,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        Workout w = new Workout(id, name, description, days, exercises);
        if (sessoes != null) w.sessoes.addAll(sessoes);
        w.createdAt = createdAt;
        w.updatedAt = updatedAt;
        return w;
    }

    /** Altera os dados da ficha aplicando as regras do domínio. */
    public final void alterar(String name, String description, List<String> days, List<Exercise> exercises) {
        if (name == null || name.trim().length() < 2 || name.trim().length() > 80) {
            throw new IllegalArgumentException("nome da ficha deve ter entre 2 e 80 caracteres");
        }
        if (days == null || days.isEmpty()) {
            throw new IllegalArgumentException("selecione pelo menos um dia da semana");
        }
        if (exercises == null || exercises.isEmpty() || exercises.size() > 30) {
            throw new IllegalArgumentException("a ficha precisa ter entre 1 e 30 exercícios");
        }
        this.name = name.trim();
        this.description = description == null ? "" : description.trim();
        this.days.clear();
        // Mantém a ordem da semana (Seg..Dom), como o frontend exibe
        Dominio.DIAS.stream().filter(days::contains).forEach(this.days::add);
        this.exercises.clear();
        this.exercises.addAll(exercises);
    }

    /** Evento vindo do progresso-service: uma sessão desta ficha foi registrada (idempotente). */
    public void registrarSessao(String sessaoId, LocalDate data) {
        sessoes.removeIf(s -> s.sessaoId().equals(sessaoId));
        sessoes.add(new SessaoRealizada(sessaoId, data));
    }

    /** Evento vindo do progresso-service: uma sessão desta ficha foi excluída (idempotente). */
    public void removerSessao(String sessaoId) {
        sessoes.removeIf(s -> s.sessaoId().equals(sessaoId));
    }

    public void marcarPersistencia(LocalDateTime agora) {
        if (createdAt == null) createdAt = agora;
        updatedAt = agora;
    }

    public BigDecimal volume() {
        return exercises.stream().map(Exercise::volume).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int totalSessoes() {
        return sessoes.size();
    }

    public LocalDate ultimaSessao() {
        return sessoes.stream().map(SessaoRealizada::data).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getDays() { return List.copyOf(days); }
    public List<Exercise> getExercises() { return List.copyOf(exercises); }
    public List<SessaoRealizada> getSessoes() { return List.copyOf(sessoes); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
