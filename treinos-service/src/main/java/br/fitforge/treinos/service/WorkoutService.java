package br.fitforge.treinos.service;

import br.fitforge.treinos.dto.EstatisticasDTO;
import br.fitforge.treinos.dto.ExerciseRequest;
import br.fitforge.treinos.dto.WorkoutRequest;
import br.fitforge.treinos.exception.ConflitoException;
import br.fitforge.treinos.exception.RecursoNaoEncontradoException;
import br.fitforge.treinos.exception.RegraNegocioException;
import br.fitforge.treinos.model.Dominio;
import br.fitforge.treinos.model.Exercise;
import br.fitforge.treinos.model.Workout;
import br.fitforge.treinos.repository.WorkoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/** Regras de negócio das fichas de treino. */
@Service
public class WorkoutService {

    private final WorkoutRepository repository;

    public WorkoutService(WorkoutRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Workout> listar(String dia, String busca) {
        if (dia != null && !dia.isBlank() && !Dominio.DIAS.contains(dia)) {
            throw new RegraNegocioException("Dia inválido. Use: " + String.join(", ", Dominio.DIAS));
        }
        String termo = busca == null ? "" : busca.trim().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(w -> dia == null || dia.isBlank() || w.getDays().contains(dia))
                .filter(w -> termo.isEmpty() || w.getName().toLowerCase(Locale.ROOT).contains(termo))
                .sorted(Comparator.comparing(Workout::getCreatedAt, Comparator.nullsLast(Comparator.<java.time.LocalDateTime>reverseOrder())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Workout buscar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ficha não encontrada: " + id));
    }

    /** Cria a ficha. O frontend pode enviar o próprio id (UUID); se não enviar, o serviço gera. */
    @Transactional
    public Workout criar(WorkoutRequest req) {
        String id = (req.id() == null || req.id().isBlank()) ? UUID.randomUUID().toString() : req.id().trim();
        if (repository.existsById(id)) {
            throw new ConflitoException("Já existe uma ficha com o id " + id);
        }
        Workout w = new Workout();
        w.setId(id);
        aplicar(w, req);
        return repository.save(w);
    }

    @Transactional
    public Workout atualizar(String id, WorkoutRequest req) {
        if (req.id() != null && !req.id().isBlank() && !req.id().equals(id)) {
            throw new RegraNegocioException("O id do corpo difere do id da URL.");
        }
        Workout w = buscar(id);
        aplicar(w, req);
        return repository.save(w);
    }

    @Transactional
    public void excluir(String id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Ficha não encontrada: " + id);
        }
        // As sessões já registradas ficam no progresso-service (database per service):
        // apagar a ficha NÃO apaga o histórico.
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public EstatisticasDTO estatisticas() {
        List<Workout> fichas = repository.findAll();

        Map<String, Long> porDia = new LinkedHashMap<>();
        Dominio.DIAS.forEach(d -> porDia.put(d, fichas.stream().filter(w -> w.getDays().contains(d)).count()));

        Map<String, Long> porGrupo = new LinkedHashMap<>();
        Dominio.GRUPOS_MUSCULARES.forEach(g -> porGrupo.put(g, 0L));
        fichas.stream().flatMap(w -> w.getExercises().stream())
                .collect(Collectors.groupingBy(Exercise::getMuscle, Collectors.counting()))
                .forEach(porGrupo::put);

        long totalExercicios = fichas.stream().mapToLong(w -> w.getExercises().size()).sum();
        BigDecimal volume = fichas.stream().map(Workout::getVolume).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new EstatisticasDTO(fichas.size(), totalExercicios, volume, porDia, porGrupo);
    }

    // ------------------------------------------------------------------

    private void aplicar(Workout w, WorkoutRequest req) {
        List<String> dias = req.days();
        if (new HashSet<>(dias).size() != dias.size()) {
            throw new RegraNegocioException("Dias da semana repetidos na ficha.");
        }
        List<Exercise> exercicios = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (ExerciseRequest e : req.exercises()) {
            String eid = (e.id() == null || e.id().isBlank()) ? UUID.randomUUID().toString() : e.id().trim();
            if (!ids.add(eid)) {
                throw new RegraNegocioException("Exercícios com id repetido: " + eid);
            }
            exercicios.add(new Exercise(eid, e.name().trim(), e.muscle(), e.sets(), e.reps(), e.load(), e.rest()));
        }
        // Mantém a ordem da semana (Seg..Dom), como o frontend exibe
        List<String> ordenados = Dominio.DIAS.stream().filter(dias::contains).toList();

        w.setName(req.name().trim());
        w.setDescription(req.description() == null ? "" : req.description().trim());
        w.getDays().clear();
        w.getDays().addAll(ordenados);
        w.getExercises().clear();
        w.getExercises().addAll(exercicios);
    }
}
