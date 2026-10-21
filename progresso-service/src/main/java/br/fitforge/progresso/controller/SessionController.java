package br.fitforge.progresso.controller;

import br.fitforge.progresso.dto.EstatisticasDTO;
import br.fitforge.progresso.dto.SessionRequest;
import br.fitforge.progresso.model.Session;
import br.fitforge.progresso.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/** CRUD de sessões de treino. */
@RestController
@RequestMapping("/api/sessoes")
@Tag(name = "Sessões de treino", description = "CRUD do histórico de treinos e indicadores de progresso")
public class SessionController {

    private final SessionService service;

    public SessionController(SessionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar sessões", description = "Mais recentes primeiro. Filtros: ?workoutId=...&periodo=semana|mes|tudo")
    public List<Session> listar(@RequestParam(required = false) String workoutId,
                                @Parameter(description = "semana, mes ou tudo") @RequestParam(required = false) String periodo) {
        return service.listar(workoutId, periodo);
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Indicadores de progresso", description = "Totais, semana atual (seg–dom), esforço médio, último peso e sequência")
    public EstatisticasDTO estatisticas() {
        return service.estatisticas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar sessão por id")
    public Session buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Registrar sessão")
    public ResponseEntity<Session> criar(@Valid @RequestBody SessionRequest req) {
        Session criada = service.criar(req);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar sessão")
    public Session atualizar(@PathVariable String id, @Valid @RequestBody SessionRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir sessão")
    public void excluir(@PathVariable String id) {
        service.excluir(id);
    }
}
