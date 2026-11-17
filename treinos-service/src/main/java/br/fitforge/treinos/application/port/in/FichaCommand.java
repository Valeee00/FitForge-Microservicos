package br.fitforge.treinos.application.port.in;

import br.fitforge.treinos.domain.model.Exercise;

import java.util.List;

/** Dados de entrada para criar/alterar uma ficha (independente de HTTP). */
public record FichaCommand(String id, String name, String description, List<String> days, List<Exercise> exercises) {}
