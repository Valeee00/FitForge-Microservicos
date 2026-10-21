package br.fitforge.progresso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * progresso-service — Bounded Context "Acompanhamento".
 * Dono exclusivo das sessões de treino registradas (database per service).
 * Guarda workoutId + workoutName como cópia (snapshot), sem depender do
 * treinos-service em tempo de execução: a composição é papel do BFF.
 */
@SpringBootApplication
public class ProgressoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProgressoApplication.class, args);
    }
}
