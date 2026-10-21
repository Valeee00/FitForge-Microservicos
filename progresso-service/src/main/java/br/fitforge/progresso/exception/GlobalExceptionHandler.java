package br.fitforge.progresso.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Converte exceções em respostas JSON padronizadas. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> naoEncontrado(RecursoNaoEncontradoException ex) {
        return corpo(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, Object>> regraNegocio(RegraNegocioException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Violação de regra de negócio", ex.getMessage(), null);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<Map<String, Object>> conflito(ConflitoException ex) {
        return corpo(HttpStatus.CONFLICT, "Conflito", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(ge -> campos.putIfAbsent(ge.getObjectName(), ge.getDefaultMessage()));
        return corpo(HttpStatus.BAD_REQUEST, "Erro de validação", "Revise os campos enviados.", campos);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> validacaoParametro(HandlerMethodValidationException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Erro de validação", "Parâmetros inválidos.", null);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> jsonInvalido(Exception ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Requisição inválida", "JSON ou parâmetro em formato inválido.", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> geral(Exception ex) {
        // Erros do próprio Spring (404 de rota, 405 de método etc.) mantêm o status original
        if (ex instanceof ErrorResponse er) {
            HttpStatusCode status = er.getStatusCode();
            return corpo(HttpStatus.valueOf(status.value()), er.getBody().getTitle(), er.getBody().getDetail(), null);
        }
        log.error("Erro inesperado", ex);
        return corpo(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor", "Tente novamente mais tarde.", null);
    }

    private ResponseEntity<Map<String, Object>> corpo(HttpStatus status, String erro, String mensagem,
                                                      Map<String, String> campos) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", erro);
        body.put("message", mensagem);
        if (campos != null) body.put("fieldErrors", campos);
        return ResponseEntity.status(status).body(body);
    }
}
