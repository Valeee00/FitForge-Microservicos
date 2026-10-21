package br.fitforge.treinos.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Exercício recebido do frontend. Regras = validExercise() de domain.js. */
public record ExerciseRequest(
        @Size(max = 80, message = "id do exercício deve ter até 80 caracteres")
        String id,

        @NotBlank(message = "nome do exercício é obrigatório")
        @Size(min = 2, max = 80, message = "nome do exercício deve ter entre 2 e 80 caracteres")
        String name,

        @NotBlank(message = "grupo muscular é obrigatório")
        @Pattern(regexp = "Peito|Costas|Pernas|Ombros|Braços|Abdômen|Corpo inteiro",
                message = "grupo muscular inválido")
        String muscle,

        @NotNull @Min(value = 1, message = "mínimo 1 série") @Max(value = 20, message = "máximo 20 séries")
        Integer sets,

        @NotNull @Min(value = 1, message = "mínimo 1 repetição") @Max(value = 100, message = "máximo 100 repetições")
        Integer reps,

        @NotNull @DecimalMin(value = "0", message = "carga não pode ser negativa")
        @DecimalMax(value = "500", message = "carga máxima 500 kg")
        BigDecimal load,

        @NotNull @Min(value = 0, message = "descanso não pode ser negativo") @Max(value = 600, message = "descanso máximo 600 s")
        Integer rest
) {}
