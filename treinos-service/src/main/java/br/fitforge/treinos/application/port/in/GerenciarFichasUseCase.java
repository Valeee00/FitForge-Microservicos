package br.fitforge.treinos.application.port.in;

import br.fitforge.treinos.domain.model.Workout;

import java.util.List;

/** Porta de entrada: casos de uso do CRUD de fichas. */
public interface GerenciarFichasUseCase {
    List<Workout> listar(String dia, String busca);

    Workout buscar(String id);

    Workout criar(FichaCommand comando);

    Workout atualizar(String id, FichaCommand comando);

    void excluir(String id);

    EstatisticasFichas estatisticas();
}
