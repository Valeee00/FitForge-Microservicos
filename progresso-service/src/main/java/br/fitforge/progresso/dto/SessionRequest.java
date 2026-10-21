package br.fitforge.progresso.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Sessão recebida em POST/PUT. Regras = validSession() de domain.js. */
public record SessionRequest(
        @Size(max = 80, message = "id deve ter até 80 caracteres")
        String id,

        @NotBlank(message = "a sessão precisa estar ligada a uma ficha")
        @Size(max = 80)
        String workoutId,

        @NotBlank(message = "nome da ficha é obrigatório")
        @Size(min = 2, max = 80, message = "nome da ficha deve ter entre 2 e 80 caracteres")
        String workoutName,

        @NotNull(message = "data é obrigatória (AAAA-MM-DD)")
        LocalDate date,

        @NotNull @Min(value = 1, message = "duração mínima 1 min") @Max(value = 360, message = "duração máxima 360 min")
        Integer duration,

        @NotNull @Min(value = 1, message = "esforço entre 1 e 10") @Max(value = 10, message = "esforço entre 1 e 10")
        Integer effort,

        @DecimalMin(value = "20", message = "peso mínimo 20 kg") @DecimalMax(value = "400", message = "peso máximo 400 kg")
        BigDecimal bodyWeight,

        @Size(max = 500, message = "observações com até 500 caracteres")
        String notes
) {}
