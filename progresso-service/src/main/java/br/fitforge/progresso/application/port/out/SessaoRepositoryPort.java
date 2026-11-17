package br.fitforge.progresso.application.port.out;

import br.fitforge.progresso.domain.model.Session;

import java.util.List;
import java.util.Optional;

/** Porta de saída: persistência das sessões (implementada pelo adaptador JPA / Azure SQL). */
public interface SessaoRepositoryPort {
    List<Session> listarTodas();

    List<Session> listarPorFicha(String workoutId);

    Optional<Session> buscarPorId(String id);

    boolean existe(String id);

    Session salvar(Session sessao);

    void salvarTodas(List<Session> sessoes);

    void excluir(String id);

    long contar();
}
