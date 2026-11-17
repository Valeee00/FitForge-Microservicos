package br.fitforge.treinos.application.port.out;

/** Porta de saída: publicação de eventos de domínio (implementada pelo adaptador Azure Service Bus). */
public interface FichaEventPublisherPort {
    void fichaRenomeada(String fichaId, String novoNome);

    void fichaExcluida(String fichaId, String nome);
}
