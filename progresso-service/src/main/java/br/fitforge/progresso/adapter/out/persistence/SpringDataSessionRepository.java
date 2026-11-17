package br.fitforge.progresso.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repositório Spring Data JPA (detalhe de infraestrutura). */
interface SpringDataSessionRepository extends JpaRepository<SessionEntity, String> {
    List<SessionEntity> findByWorkoutId(String workoutId);
}
