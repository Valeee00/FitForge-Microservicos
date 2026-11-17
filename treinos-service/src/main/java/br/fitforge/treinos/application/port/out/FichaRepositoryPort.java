package br.fitforge.treinos.application.port.out;

import br.fitforge.treinos.domain.model.Workout;

import java.util.List;
import java.util.Optional;

/** Porta de saída: persistência das fichas (implementada pelo adaptador MongoDB). */
public interface FichaRepositoryPort {
    List<Workout> listarTodas();

    Optional<Workout> buscarPorId(String id);

    boolean existe(String id);

    Workout salvar(Workout ficha);

    void excluir(String id);

    long contar();
}
