package br.fitforge.treinos.adapter.in.web;

import br.fitforge.treinos.adapter.in.web.dto.WorkoutRequest;
import br.fitforge.treinos.adapter.in.web.dto.WorkoutResponse;
import br.fitforge.treinos.application.port.in.EstatisticasFichas;
import br.fitforge.treinos.application.port.in.GerenciarFichasUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/** Adaptador de entrada HTTP: CRUD de fichas de treino. */
@RestController
@RequestMapping("/api/treinos")
@Tag(name = "Fichas de treino", description = "CRUD das fichas e exercícios do FitForge (MongoDB)")
public class WorkoutController {

    private final GerenciarFichasUseCase fichas;

    public WorkoutController(GerenciarFichasUseCase fichas) {
        this.fichas = fichas;
    }

    @GetMapping
    @Operation(summary = "Listar fichas", description = "Filtros opcionais: ?dia=Seg e ?busca=texto")
    public List<WorkoutResponse> listar(@Parameter(description = "Seg, Ter, Qua, Qui, Sex, Sáb ou Dom")
                                        @RequestParam(required = false) String dia,
                                        @RequestParam(required = false) String busca) {
        return fichas.listar(dia, busca).stream().map(WorkoutWebMapper::toResponse).toList();
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Indicadores das fichas", description = "Totais, volume, sessões realizadas e distribuição por dia e grupo muscular")
    public EstatisticasFichas estatisticas() {
        return fichas.estatisticas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ficha por id")
    public WorkoutResponse buscar(@PathVariable String id) {
        return WorkoutWebMapper.toResponse(fichas.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Criar ficha")
    public ResponseEntity<WorkoutResponse> criar(@Valid @RequestBody WorkoutRequest req) {
        WorkoutResponse criada = WorkoutWebMapper.toResponse(fichas.criar(WorkoutWebMapper.toCommand(req)));
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar ficha", description = "Se o nome mudar, publica FICHA_RENOMEADA no Azure Service Bus")
    public WorkoutResponse atualizar(@PathVariable String id, @Valid @RequestBody WorkoutRequest req) {
        return WorkoutWebMapper.toResponse(fichas.atualizar(id, WorkoutWebMapper.toCommand(req)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir ficha", description = "Publica FICHA_EXCLUIDA no Azure Service Bus; as sessões são preservadas no progresso-service")
    public void excluir(@PathVariable String id) {
        fichas.excluir(id);
    }
}
