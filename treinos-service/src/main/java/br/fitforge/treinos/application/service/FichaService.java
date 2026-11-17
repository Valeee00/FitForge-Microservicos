package br.fitforge.treinos.application.service;

import br.fitforge.treinos.application.port.in.EstatisticasFichas;
import br.fitforge.treinos.application.port.in.FichaCommand;
import br.fitforge.treinos.application.port.in.GerenciarFichasUseCase;
import br.fitforge.treinos.application.port.in.ProcessarEventoSessaoUseCase;
import br.fitforge.treinos.application.port.out.FichaEventPublisherPort;
import br.fitforge.treinos.application.port.out.FichaRepositoryPort;
import br.fitforge.treinos.domain.exception.ConflitoException;
import br.fitforge.treinos.domain.exception.RecursoNaoEncontradoException;
import br.fitforge.treinos.domain.exception.RegraNegocioException;
import br.fitforge.treinos.domain.model.Dominio;
import br.fitforge.treinos.domain.model.Exercise;
import br.fitforge.treinos.domain.model.Workout;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço de aplicação: orquestra os casos de uso usando apenas as portas.
 * Não depende de Spring — é instanciado em config/BeanConfig (inversão de dependência).
 */
public class FichaService implements GerenciarFichasUseCase, ProcessarEventoSessaoUseCase {

    private final FichaRepositoryPort repositorio;
    private final FichaEventPublisherPort eventos;
    private final Clock clock;

    public FichaService(FichaRepositoryPort repositorio, FichaEventPublisherPort eventos, Clock clock) {
        this.repositorio = repositorio;
        this.eventos = eventos;
        this.clock = clock;
    }

    @Override
    public List<Workout> listar(String dia, String busca) {
        if (dia != null && !dia.isBlank() && !Dominio.DIAS.contains(dia)) {
            throw new RegraNegocioException("Dia inválido. Use: " + String.join(", ", Dominio.DIAS));
        }
        String termo = busca == null ? "" : busca.trim().toLowerCase(Locale.ROOT);
        return repositorio.listarTodas().stream()
                .filter(w -> dia == null || dia.isBlank() || w.getDays().contains(dia))
                .filter(w -> termo.isEmpty() || w.getName().toLowerCase(Locale.ROOT).contains(termo))
                .sorted(Comparator.comparing(Workout::getCreatedAt,
                        Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder())))
                .toList();
    }

    @Override
    public Workout buscar(String id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ficha não encontrada: " + id));
    }

    @Override
    public Workout criar(FichaCommand comando) {
        String id = (comando.id() == null || comando.id().isBlank()) ? UUID.randomUUID().toString() : comando.id().trim();
        if (repositorio.existe(id)) {
            throw new ConflitoException("Já existe uma ficha com o id " + id);
        }
        Workout ficha = novaFicha(id, comando);
        ficha.marcarPersistencia(LocalDateTime.now(clock));
        return repositorio.salvar(ficha);
    }

    @Override
    public Workout atualizar(String id, FichaCommand comando) {
        if (comando.id() != null && !comando.id().isBlank() && !comando.id().equals(id)) {
            throw new RegraNegocioException("O id do corpo difere do id da URL.");
        }
        Workout ficha = buscar(id);
        String nomeAnterior = ficha.getName();
        validar(comando);
        try {
            ficha.alterar(comando.name(), comando.description(), comando.days(), normalizar(comando.exercises()));
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException(e.getMessage());
        }
        ficha.marcarPersistencia(LocalDateTime.now(clock));
        Workout salva = repositorio.salvar(ficha);
        if (!salva.getName().equals(nomeAnterior)) {
            eventos.fichaRenomeada(salva.getId(), salva.getName());
        }
        return salva;
    }

    @Override
    public void excluir(String id) {
        Workout ficha = buscar(id);
        repositorio.excluir(id);
        // As sessões continuam no progresso-service (database per service); ele é avisado por mensagem.
        eventos.fichaExcluida(id, ficha.getName());
    }

    @Override
    public EstatisticasFichas estatisticas() {
        List<Workout> fichas = repositorio.listarTodas();

        Map<String, Long> porDia = new LinkedHashMap<>();
        Dominio.DIAS.forEach(d -> porDia.put(d, fichas.stream().filter(w -> w.getDays().contains(d)).count()));

        Map<String, Long> porGrupo = new LinkedHashMap<>();
        Dominio.GRUPOS_MUSCULARES.forEach(g -> porGrupo.put(g, 0L));
        fichas.stream().flatMap(w -> w.getExercises().stream())
                .collect(Collectors.groupingBy(Exercise::muscle, Collectors.counting()))
                .forEach(porGrupo::put);

        long totalExercicios = fichas.stream().mapToLong(w -> w.getExercises().size()).sum();
        BigDecimal volume = fichas.stream().map(Workout::volume).reduce(BigDecimal.ZERO, BigDecimal::add);
        long sessoes = fichas.stream().mapToLong(Workout::totalSessoes).sum();
        return new EstatisticasFichas(fichas.size(), totalExercicios, volume, sessoes, porDia, porGrupo);
    }

    // ------------------------------------------------- eventos do progresso-service

    @Override
    public void sessaoRegistrada(String sessaoId, String fichaId, LocalDate data) {
        repositorio.buscarPorId(fichaId).ifPresent(ficha -> {
            ficha.registrarSessao(sessaoId, data);
            repositorio.salvar(ficha);
        });
    }

    @Override
    public void sessaoExcluida(String sessaoId, String fichaId) {
        repositorio.buscarPorId(fichaId).ifPresent(ficha -> {
            ficha.removerSessao(sessaoId);
            repositorio.salvar(ficha);
        });
    }

    // ------------------------------------------------- auxiliares

    private Workout novaFicha(String id, FichaCommand comando) {
        validar(comando);
        try {
            return new Workout(id, comando.name(), comando.description(), comando.days(), normalizar(comando.exercises()));
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException(e.getMessage());
        }
    }

    private void validar(FichaCommand comando) {
        List<String> dias = comando.days() == null ? List.of() : comando.days();
        if (new HashSet<>(dias).size() != dias.size()) {
            throw new RegraNegocioException("Dias da semana repetidos na ficha.");
        }
        if (!Dominio.DIAS.containsAll(dias)) {
            throw new RegraNegocioException("Dia inválido. Use: " + String.join(", ", Dominio.DIAS));
        }
    }

    private List<Exercise> normalizar(List<Exercise> exercicios) {
        if (exercicios == null) return List.of();
        List<Exercise> lista = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (Exercise e : exercicios) {
            String eid = (e.id() == null || e.id().isBlank()) ? UUID.randomUUID().toString() : e.id().trim();
            if (!ids.add(eid)) {
                throw new RegraNegocioException("Exercícios com id repetido: " + eid);
            }
            lista.add(new Exercise(eid, e.name().trim(), e.muscle(), e.sets(), e.reps(), e.load(), e.rest()));
        }
        return lista;
    }
}
