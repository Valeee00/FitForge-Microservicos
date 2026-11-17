package br.fitforge.treinos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * treinos-service — Microsserviço 1 (MongoDB), Bounded Context "Planejamento".
 *
 * Arquitetura Hexagonal (Ports & Adapters):
 *   domain      → regras de negócio puras (Workout, Exercise)
 *   application → casos de uso + portas (in/out)
 *   adapter     → HTTP (in/web), Service Bus (in/messaging, out/messaging), MongoDB (out/persistence)
 *   config      → composição (liga portas e adaptadores)
 */
@SpringBootApplication
public class TreinosApplication {
    public static void main(String[] args) {
        SpringApplication.run(TreinosApplication.class, args);
    }
}
