package br.fitforge.treinos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/** Ficha recebida em POST/PUT. Regras = validWorkout() de domain.js. */
public record WorkoutRequest(
        @Size(max = 80, message = "id deve ter até 80 caracteres")
        String id,

        @NotBlank(message = "nome da ficha é obrigatório")
        @Size(min = 2, max = 80, message = "nome da ficha deve ter entre 2 e 80 caracteres")
        String name,

        @Size(max = 250, message = "descrição deve ter até 250 caracteres")
        String description,

        @NotEmpty(message = "selecione pelo menos um dia da semana")
        List<@NotBlank @Pattern(regexp = "Seg|Ter|Qua|Qui|Sex|Sáb|Dom", message = "dia inválido") String> days,

        @NotEmpty(message = "a ficha precisa de pelo menos um exercício")
        @Size(max = 30, message = "máximo de 30 exercícios por ficha")
        List<@Valid @NotNull ExerciseRequest> exercises
) {}
