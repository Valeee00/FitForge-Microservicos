package br.fitforge.progresso.application.port.in;

import br.fitforge.progresso.domain.model.Session;

import java.util.List;

/** Porta de entrada: casos de uso do CRUD de sessões. */
public interface GerenciarSessoesUseCase {
    List<Session> listar(String workoutId, String periodo);

    Session buscar(String id);

    Session criar(SessaoCommand comando);

    Session atualizar(String id, SessaoCommand comando);

    void excluir(String id);

    EstatisticasProgresso estatisticas();
}
