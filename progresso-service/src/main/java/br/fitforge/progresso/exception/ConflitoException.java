package br.fitforge.progresso.exception;

/** O recurso já existe (HTTP 409). */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
