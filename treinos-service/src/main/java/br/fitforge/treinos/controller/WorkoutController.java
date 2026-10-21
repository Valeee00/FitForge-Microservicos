package br.fitforge.treinos.controller;

import br.fitforge.treinos.dto.EstatisticasDTO;
import br.fitforge.treinos.dto.WorkoutRequest;
import br.fitforge.treinos.model.Workout;
import br.fitforge.treinos.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/** CRUD de fichas de treino. */
@RestController
@RequestMapping("/api/treinos")
@Tag(name = "Fichas de treino", description = "CRUD das fichas e exercícios do FitForge")
public class WorkoutController {

    private final WorkoutService service;

    public WorkoutController(WorkoutService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar fichas", description = "Filtros opcionais: ?dia=Seg e ?busca=texto")
    public List<Workout> listar(@Parameter(description = "Seg, Ter, Qua, Qui, Sex, Sáb ou Dom") @RequestParam(required = false) String dia,
                                @RequestParam(required = false) String busca) {
        return service.listar(dia, busca);
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Indicadores das fichas", description = "Totais, volume e distribuição por dia e grupo muscular")
    public EstatisticasDTO estatisticas() {
        return service.estatisticas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ficha por id")
    public Workout buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Criar ficha")
    public ResponseEntity<Workout> criar(@Valid @RequestBody WorkoutRequest req) {
        Workout criado = service.criar(req);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar ficha")
    public Workout atualizar(@PathVariable String id, @Valid @RequestBody WorkoutRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir ficha", description = "As sessões já registradas no progresso-service são preservadas")
    public void excluir(@PathVariable String id) {
        service.excluir(id);
    }
}
