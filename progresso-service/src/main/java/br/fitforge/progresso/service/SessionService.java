package br.fitforge.progresso.service;

import br.fitforge.progresso.dto.DiaSemanaDTO;
import br.fitforge.progresso.dto.EstatisticasDTO;
import br.fitforge.progresso.dto.SessionRequest;
import br.fitforge.progresso.exception.ConflitoException;
import br.fitforge.progresso.exception.RecursoNaoEncontradoException;
import br.fitforge.progresso.exception.RegraNegocioException;
import br.fitforge.progresso.model.Session;
import br.fitforge.progresso.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/** Regras de negócio das sessões de treino. */
@Service
public class SessionService {

    private static final List<String> DIAS = List.of("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom");

    private final SessionRepository repository;
    private final Clock clock;

    public SessionService(SessionRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /**
     * Lista sessões (mais recentes primeiro).
     * @param workoutId filtra por ficha
     * @param periodo   "semana" (seg–dom atual), "mes" (mês atual) ou vazio (tudo)
     */
    @Transactional(readOnly = true)
    public List<Session> listar(String workoutId, String periodo) {
        List<Session> base;
        if (workoutId != null && !workoutId.isBlank()) {
            base = repository.findByWorkoutIdOrderByDateDescCreatedAtDesc(workoutId);
        } else {
            base = repository.findAllByOrderByDateDescCreatedAtDesc();
        }
        if (periodo == null || periodo.isBlank() || periodo.equalsIgnoreCase("tudo")) return base;

        LocalDate hoje = LocalDate.now(clock);
        LocalDate inicio = switch (periodo.toLowerCase(Locale.ROOT)) {
            case "semana" -> hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case "mes" -> hoje.withDayOfMonth(1);
            default -> throw new RegraNegocioException("Período inválido. Use semana, mes ou tudo.");
        };
        LocalDate fim = periodo.equalsIgnoreCase("semana") ? inicio.plusDays(6) : hoje.withDayOfMonth(hoje.lengthOfMonth());
        return base.stream().filter(s -> !s.getDate().isBefore(inicio) && !s.getDate().isAfter(fim)).toList();
    }

    @Transactional(readOnly = true)
    public Session buscar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sessão não encontrada: " + id));
    }

    @Transactional
    public Session criar(SessionRequest req) {
        String id = (req.id() == null || req.id().isBlank()) ? UUID.randomUUID().toString() : req.id().trim();
        if (repository.existsById(id)) {
            throw new ConflitoException("Já existe uma sessão com o id " + id);
        }
        Session s = new Session();
        s.setId(id);
        aplicar(s, req);
        return repository.save(s);
    }

    @Transactional
    public Session atualizar(String id, SessionRequest req) {
        if (req.id() != null && !req.id().isBlank() && !req.id().equals(id)) {
            throw new RegraNegocioException("O id do corpo difere do id da URL.");
        }
        Session s = buscar(id);
        aplicar(s, req);
        return repository.save(s);
    }

    @Transactional
    public void excluir(String id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Sessão não encontrada: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public EstatisticasDTO estatisticas() {
        List<Session> todas = repository.findAllByOrderByDateDescCreatedAtDesc();
        LocalDate hoje = LocalDate.now(clock);
        LocalDate segunda = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<DiaSemanaDTO> semana = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate dia = segunda.plusDays(i);
            List<Session> doDia = todas.stream().filter(s -> s.getDate().equals(dia)).toList();
            semana.add(new DiaSemanaDTO(dia, DIAS.get(i), doDia.size(),
                    doDia.stream().mapToLong(Session::getDuration).sum()));
        }

        long minutos = todas.stream().mapToLong(Session::getDuration).sum();
        long sessoesMes = todas.stream()
                .filter(s -> s.getDate().getYear() == hoje.getYear() && s.getDate().getMonth() == hoje.getMonth())
                .count();
        Double esforco = todas.isEmpty() ? null : BigDecimal.valueOf(
                todas.stream().mapToInt(Session::getEffort).average().orElse(0))
                .setScale(1, RoundingMode.HALF_UP).doubleValue();
        BigDecimal ultimoPeso = todas.stream().map(Session::getBodyWeight).filter(Objects::nonNull).findFirst().orElse(null);

        Map<String, Long> porFicha = todas.stream()
                .collect(Collectors.groupingBy(Session::getWorkoutName, LinkedHashMap::new, Collectors.counting()));

        return new EstatisticasDTO(
                todas.size(), minutos,
                semana.stream().mapToLong(DiaSemanaDTO::count).sum(),
                semana.stream().mapToLong(DiaSemanaDTO::minutes).sum(),
                sessoesMes, esforco, ultimoPeso,
                todas.isEmpty() ? null : todas.get(0).getDate(),
                sequencia(todas, hoje), semana, porFicha);
    }

    // ------------------------------------------------------------------

    /** Dias consecutivos com treino terminando hoje (ou ontem, se ainda não treinou hoje). */
    private int sequencia(List<Session> todas, LocalDate hoje) {
        Set<LocalDate> datas = todas.stream().map(Session::getDate).collect(Collectors.toSet());
        LocalDate dia = datas.contains(hoje) ? hoje : hoje.minusDays(1);
        int n = 0;
        while (datas.contains(dia)) {
            n++;
            dia = dia.minusDays(1);
        }
        return n;
    }

    private void aplicar(Session s, SessionRequest req) {
        if (req.date().isAfter(LocalDate.now(clock))) {
            throw new RegraNegocioException("A data da sessão não pode estar no futuro.");
        }
        s.setWorkoutId(req.workoutId().trim());
        s.setWorkoutName(req.workoutName().trim());
        s.setDate(req.date());
        s.setDuration(req.duration());
        s.setEffort(req.effort());
        s.setBodyWeight(req.bodyWeight());
        s.setNotes(req.notes() == null ? "" : req.notes().trim());
    }
}
