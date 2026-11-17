package br.fitforge.progresso.application.port.in;

/** Porta de entrada assíncrona: eventos de ficha publicados pelo treinos-service. */
public interface ProcessarEventoFichaUseCase {
    void fichaRenomeada(String fichaId, String novoNome);

    void fichaExcluida(String fichaId);
}
