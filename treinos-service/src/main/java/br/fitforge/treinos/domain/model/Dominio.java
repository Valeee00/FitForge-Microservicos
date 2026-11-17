package br.fitforge.treinos.domain.model;

import java.util.List;

/** Vocabulário do domínio — idêntico a packages/shared/src/domain.js do frontend. */
public final class Dominio {
    public static final List<String> DIAS = List.of("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom");
    public static final List<String> GRUPOS_MUSCULARES =
            List.of("Peito", "Costas", "Pernas", "Ombros", "Braços", "Abdômen", "Corpo inteiro");

    private Dominio() {
    }
}
