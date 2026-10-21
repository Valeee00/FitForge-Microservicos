package br.fitforge.treinos.exception;

/** Violação de regra de negócio (HTTP 400). */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
