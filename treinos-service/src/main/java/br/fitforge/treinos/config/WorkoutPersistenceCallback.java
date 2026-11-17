package br.fitforge.treinos.config;

import br.fitforge.treinos.model.Workout;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
public class WorkoutPersistenceCallback implements BeforeConvertCallback<Workout> {

    @Override
    public Workout onBeforeConvert(Workout workout, String collection) {
        workout.prepararPersistencia();
        return workout;
    }
}