package br.fitforge.treinos.adapter.out.persistence;

import br.fitforge.treinos.application.port.out.FichaRepositoryPort;
import br.fitforge.treinos.domain.model.Exercise;
import br.fitforge.treinos.domain.model.SessaoRealizada;
import br.fitforge.treinos.domain.model.Workout;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adaptador de saída: implementa a porta de persistência usando MongoDB. */
@Component
public class MongoFichaRepositoryAdapter implements FichaRepositoryPort {

    private final SpringDataWorkoutRepository repository;

    MongoFichaRepositoryAdapter(SpringDataWorkoutRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Workout> listarTodas() {
        return repository.findAll().stream().map(MongoFichaRepositoryAdapter::toDomain).toList();
    }

    @Override
    public Optional<Workout> buscarPorId(String id) {
        return repository.findById(id).map(MongoFichaRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existe(String id) {
        return repository.existsById(id);
    }

    @Override
    public Workout salvar(Workout ficha) {
        return toDomain(repository.save(toDocument(ficha)));
    }

    @Override
    public void excluir(String id) {
        repository.deleteById(id);
    }

    @Override
    public long contar() {
        return repository.count();
    }

    static WorkoutDocument toDocument(Workout w) {
        WorkoutDocument d = new WorkoutDocument();
        d.setId(w.getId());
        d.setName(w.getName());
        d.setDescription(w.getDescription());
        d.setDays(new ArrayList<>(w.getDays()));
        List<WorkoutDocument.ExerciseDocument> exs = new ArrayList<>();
        for (Exercise e : w.getExercises()) {
            WorkoutDocument.ExerciseDocument ed = new WorkoutDocument.ExerciseDocument();
            ed.setId(e.id());
            ed.setName(e.name());
            ed.setMuscle(e.muscle());
            ed.setSets(e.sets());
            ed.setReps(e.reps());
            ed.setLoad(e.load());
            ed.setRest(e.rest());
            exs.add(ed);
        }
        d.setExercises(exs);
        List<WorkoutDocument.SessaoDocument> sessoes = new ArrayList<>();
        for (SessaoRealizada s : w.getSessoes()) {
            WorkoutDocument.SessaoDocument sd = new WorkoutDocument.SessaoDocument();
            sd.setSessaoId(s.sessaoId());
            sd.setData(s.data());
            sessoes.add(sd);
        }
        d.setSessoes(sessoes);
        d.setCreatedAt(w.getCreatedAt());
        d.setUpdatedAt(w.getUpdatedAt());
        return d;
    }

    static Workout toDomain(WorkoutDocument d) {
        List<Exercise> exs = d.getExercises() == null ? List.of() : d.getExercises().stream()
                .map(e -> new Exercise(e.getId(), e.getName(), e.getMuscle(), e.getSets(), e.getReps(), e.getLoad(), e.getRest()))
                .toList();
        List<SessaoRealizada> sessoes = d.getSessoes() == null ? List.of() : d.getSessoes().stream()
                .map(s -> new SessaoRealizada(s.getSessaoId(), s.getData()))
                .toList();
        return Workout.reconstituir(d.getId(), d.getName(), d.getDescription(),
                d.getDays() == null ? List.of() : d.getDays(), exs, sessoes, d.getCreatedAt(), d.getUpdatedAt());
    }
}
