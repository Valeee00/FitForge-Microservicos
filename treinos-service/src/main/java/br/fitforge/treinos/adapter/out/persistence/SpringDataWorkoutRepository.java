package br.fitforge.treinos.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

/** Repositório Spring Data MongoDB (detalhe de infraestrutura). */
interface SpringDataWorkoutRepository extends MongoRepository<WorkoutDocument, String> {
}
