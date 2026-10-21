package br.fitforge.treinos.repository;

import br.fitforge.treinos.model.Workout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepository extends JpaRepository<Workout, String> {
}
