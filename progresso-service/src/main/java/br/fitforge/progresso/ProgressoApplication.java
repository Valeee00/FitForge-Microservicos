package br.fitforge.progresso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * progresso-service — Microsserviço 2 (Azure SQL Database), Bounded Context "Acompanhamento".
 *
 * Arquitetura Hexagonal (Ports & Adapters):
 *   domain      → regras de negócio puras (Session)
 *   application → casos de uso + portas (in/out)
 *   adapter     → HTTP (in/web), Service Bus (in/messaging, out/messaging), JPA/Azure SQL (out/persistence)
 *   config      → composição (liga portas e adaptadores)
 */
@SpringBootApplication
public class ProgressoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProgressoApplication.class, args);
    }
}
