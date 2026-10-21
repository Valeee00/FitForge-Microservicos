package br.fitforge.treinos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * treinos-service — Bounded Context "Planejamento".
 * Dono exclusivo das fichas de treino e seus exercícios (database per service).
 */
@SpringBootApplication
public class TreinosApplication {
    public static void main(String[] args) {
        SpringApplication.run(TreinosApplication.class, args);
    }
}
