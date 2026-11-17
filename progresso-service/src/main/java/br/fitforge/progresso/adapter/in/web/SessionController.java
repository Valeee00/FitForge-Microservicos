package br.fitforge.progresso.adapter.in.web;

import br.fitforge.progresso.adapter.in.web.dto.SessionRequest;
import br.fitforge.progresso.adapter.in.web.dto.SessionResponse;
import br.fitforge.progresso.application.port.in.EstatisticasProgresso;
import br.fitforge.progresso.application.port.in.GerenciarSessoesUseCase;
import br.fitforge.progresso.application.port.in.SessaoCommand;
import br.fitforge.progresso.domain.model.Session;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/** Adaptador de entrada HTTP: CRUD de sessões de treino. */
@RestController
@RequestMapping("/api/sessoes")
@Tag(name = "Sessões de treino", description = "CRUD do histórico de treinos (Azure SQL) e indicadores de progresso")
public class SessionController {

    private final GerenciarSessoesUseCase sessoes;

    public SessionController(GerenciarSessoesUseCase sessoes) {
        this.sessoes = sessoes;
    }

    @GetMapping
    @Operation(summary = "Listar sessões", description = "Mais recentes primeiro. Filtros: ?workoutId=...&periodo=semana|mes|tudo")
    public List<SessionResponse> listar(@RequestParam(required = false) String workoutId,
                                        @Parameter(description = "semana, mes ou tudo") @RequestParam(required = false) String periodo) {
        return sessoes.listar(workoutId, periodo).stream().map(SessionController::toResponse).toList();
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Indicadores de progresso", description = "Totais, semana atual (seg–dom), esforço médio, último peso e sequência")
    public EstatisticasProgresso estatisticas() {
        return sessoes.estatisticas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar sessão por id")
    public SessionResponse buscar(@PathVariable String id) {
        return toResponse(sessoes.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Registrar sessão", description = "Publica SESSAO_REGISTRADA no Azure Service Bus")
    public ResponseEntity<SessionResponse> criar(@Valid @RequestBody SessionRequest req) {
        SessionResponse criada = toResponse(sessoes.criar(toCommand(req)));
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar sessão")
    public SessionResponse atualizar(@PathVariable String id, @Valid @RequestBody SessionRequest req) {
        return toResponse(sessoes.atualizar(id, toCommand(req)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir sessão", description = "Publica SESSAO_EXCLUIDA no Azure Service Bus")
    public void excluir(@PathVariable String id) {
        sessoes.excluir(id);
    }

    private static SessaoCommand toCommand(SessionRequest r) {
        return new SessaoCommand(r.id(), r.workoutId(), r.workoutName(), r.date(), r.duration(), r.effort(),
                r.bodyWeight(), r.notes());
    }

    private static SessionResponse toResponse(Session s) {
        return new SessionResponse(s.getId(), s.getWorkoutId(), s.getWorkoutName(), s.getDate(), s.getDuration(),
                s.getEffort(), s.getBodyWeight(), s.getNotes(), s.isFichaAtiva(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
