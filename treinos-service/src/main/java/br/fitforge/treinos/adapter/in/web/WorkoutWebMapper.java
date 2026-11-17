package br.fitforge.treinos.adapter.in.web;

import br.fitforge.treinos.adapter.in.web.dto.ExerciseRequest;
import br.fitforge.treinos.adapter.in.web.dto.ExerciseResponse;
import br.fitforge.treinos.adapter.in.web.dto.WorkoutRequest;
import br.fitforge.treinos.adapter.in.web.dto.WorkoutResponse;
import br.fitforge.treinos.application.port.in.FichaCommand;
import br.fitforge.treinos.domain.model.Exercise;
import br.fitforge.treinos.domain.model.Workout;

import java.util.List;

/** Converte entre o contrato HTTP e o modelo do domínio. */
final class WorkoutWebMapper {

    private WorkoutWebMapper() {
    }

    static FichaCommand toCommand(WorkoutRequest r) {
        List<Exercise> exercicios = r.exercises().stream().map(WorkoutWebMapper::toExercise).toList();
        return new FichaCommand(r.id(), r.name(), r.description(), r.days(), exercicios);
    }

    private static Exercise toExercise(ExerciseRequest e) {
        return new Exercise(e.id(), e.name(), e.muscle(), e.sets(), e.reps(), e.load(), e.rest());
    }

    static WorkoutResponse toResponse(Workout w) {
        List<ExerciseResponse> exs = w.getExercises().stream()
                .map(e -> new ExerciseResponse(e.id(), e.name(), e.muscle(), e.sets(), e.reps(), e.load(), e.rest()))
                .toList();
        return new WorkoutResponse(w.getId(), w.getName(), w.getDescription(), w.getDays(), exs,
                w.totalSessoes(), w.ultimaSessao(), w.getCreatedAt(), w.getUpdatedAt());
    }
}
