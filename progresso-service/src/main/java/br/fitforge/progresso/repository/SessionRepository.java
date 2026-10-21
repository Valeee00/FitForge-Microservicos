package br.fitforge.progresso.repository;

import br.fitforge.progresso.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, String> {

    List<Session> findAllByOrderByDateDescCreatedAtDesc();

    List<Session> findByWorkoutIdOrderByDateDescCreatedAtDesc(String workoutId);

    List<Session> findByDateBetweenOrderByDateDescCreatedAtDesc(LocalDate inicio, LocalDate fim);
}
