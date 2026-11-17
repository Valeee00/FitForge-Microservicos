package br.fitforge.treinos.repository;

import br.fitforge.treinos.model.Workout;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkoutRepository extends MongoRepository<Workout, String> {
}