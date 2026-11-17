package br.fitforge.progresso.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Sessão de treino realizada — agregado raiz do Bounded Context "Acompanhamento".
 * Guarda workoutId + workoutName como cópia (snapshot): o progresso-service não
 * consulta o banco do treinos-service (database per service). Mudanças na ficha
 * chegam por mensagem (Azure Service Bus).
 */
public class Session {

    private final String id;
    private String workoutId;
    private String workoutName;
    private LocalDate date;
    private int duration;
    private int effort;
    private BigDecimal bodyWeight;
    private String notes;
    private boolean fichaAtiva = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Session(String id, String workoutId, String workoutName, LocalDate date, int duration, int effort,
                   BigDecimal bodyWeight, String notes, LocalDate hoje) {
        this.id = Objects.requireNonNull(id, "id");
        alterar(workoutId, workoutName, date, duration, effort, bodyWeight, notes, hoje);
    }

    public static Session reconstituir(String id, String workoutId, String workoutName, LocalDate date, int duration,
                                       int effort, BigDecimal bodyWeight, String notes, boolean fichaAtiva,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        Session s = new Session(id, workoutId, workoutName, date, duration, effort, bodyWeight, notes, null);
        s.fichaAtiva = fichaAtiva;
        s.createdAt = createdAt;
        s.updatedAt = updatedAt;
        return s;
    }

    /** Regras de negócio da sessão (iguais às de domain.js no frontend). */
    public final void alterar(String workoutId, String workoutName, LocalDate date, int duration, int effort,
                              BigDecimal bodyWeight, String notes, LocalDate hoje) {
        if (workoutId == null || workoutId.isBlank()) throw new IllegalArgumentException("a sessão precisa estar ligada a uma ficha");
        if (workoutName == null || workoutName.trim().length() < 2) throw new IllegalArgumentException("nome da ficha é obrigatório");
        if (date == null) throw new IllegalArgumentException("data é obrigatória");
        if (hoje != null && date.isAfter(hoje)) throw new IllegalArgumentException("A data da sessão não pode estar no futuro.");
        if (duration < 1 || duration > 360) throw new IllegalArgumentException("duração entre 1 e 360 minutos");
        if (effort < 1 || effort > 10) throw new IllegalArgumentException("esforço entre 1 e 10");
        this.workoutId = workoutId.trim();
        this.workoutName = workoutName.trim();
        this.date = date;
        this.duration = duration;
        this.effort = effort;
        this.bodyWeight = bodyWeight;
        this.notes = notes == null ? "" : notes.trim();
    }

    /** Evento FICHA_RENOMEADA: atualiza a cópia do nome. */
    public void fichaRenomeada(String novoNome) {
        this.workoutName = novoNome;
    }

    /** Evento FICHA_EXCLUIDA: a sessão continua no histórico, marcada como de ficha excluída. */
    public void fichaExcluida() {
        this.fichaAtiva = false;
    }

    public void marcarPersistencia(LocalDateTime agora) {
        if (createdAt == null) createdAt = agora;
        updatedAt = agora;
    }

    public String getId() { return id; }
    public String getWorkoutId() { return workoutId; }
    public String getWorkoutName() { return workoutName; }
    public LocalDate getDate() { return date; }
    public int getDuration() { return duration; }
    public int getEffort() { return effort; }
    public BigDecimal getBodyWeight() { return bodyWeight; }
    public String getNotes() { return notes; }
    public boolean isFichaAtiva() { return fichaAtiva; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
