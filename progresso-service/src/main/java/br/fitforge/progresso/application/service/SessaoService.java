package br.fitforge.progresso.application.service;

import br.fitforge.progresso.application.port.in.EstatisticasProgresso;
import br.fitforge.progresso.application.port.in.GerenciarSessoesUseCase;
import br.fitforge.progresso.application.port.in.ProcessarEventoFichaUseCase;
import br.fitforge.progresso.application.port.in.SessaoCommand;
import br.fitforge.progresso.application.port.out.SessaoEventPublisherPort;
import br.fitforge.progresso.application.port.out.SessaoRepositoryPort;
import br.fitforge.progresso.domain.exception.ConflitoException;
import br.fitforge.progresso.domain.exception.RecursoNaoEncontradoException;
import br.fitforge.progresso.domain.exception.RegraNegocioException;
import br.fitforge.progresso.domain.model.DiaSemana;
import br.fitforge.progresso.domain.model.Session;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço de aplicação do progresso: orquestra os casos de uso usando apenas as portas.
 * Não depende de Spring — é instanciado em config/BeanConfig.
 */
public class SessaoService implements GerenciarSessoesUseCase, ProcessarEventoFichaUseCase {

    private static final List<String> DIAS = List.of("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom");
    private static final Comparator<Session> MAIS_RECENTE_PRIMEIRO = Comparator
            .comparing(Session::getDate, Comparator.reverseOrder())
            .thenComparing(Session::getCreatedAt, Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder()));

    private final SessaoRepositoryPort repositorio;
    private final SessaoEventPublisherPort eventos;
    private final Clock clock;

    public SessaoService(SessaoRepositoryPort repositorio, SessaoEventPublisherPort eventos, Clock clock) {
        this.repositorio = repositorio;
        this.eventos = eventos;
        this.clock = clock;
    }

    @Override
    public List<Session> listar(String workoutId, String periodo) {
        List<Session> base = (workoutId != null && !workoutId.isBlank())
                ? repositorio.listarPorFicha(workoutId)
                : repositorio.listarTodas();
        base = base.stream().sorted(MAIS_RECENTE_PRIMEIRO).toList();
        if (periodo == null || periodo.isBlank() || periodo.equalsIgnoreCase("tudo")) return base;

        LocalDate hoje = LocalDate.now(clock);
        LocalDate inicio;
        LocalDate fim;
        switch (periodo.toLowerCase(Locale.ROOT)) {
            case "semana" -> {
                inicio = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                fim = inicio.plusDays(6);
            }
            case "mes" -> {
                inicio = hoje.withDayOfMonth(1);
                fim = hoje.withDayOfMonth(hoje.lengthOfMonth());
            }
            default -> throw new RegraNegocioException("Período inválido. Use semana, mes ou tudo.");
        }
        return base.stream().filter(s -> !s.getDate().isBefore(inicio) && !s.getDate().isAfter(fim)).toList();
    }

    @Override
    public Session buscar(String id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sessão não encontrada: " + id));
    }

    @Override
    public Session criar(SessaoCommand c) {
        String id = (c.id() == null || c.id().isBlank()) ? UUID.randomUUID().toString() : c.id().trim();
        if (repositorio.existe(id)) {
            throw new ConflitoException("Já existe uma sessão com o id " + id);
        }
        Session s;
        try {
            s = new Session(id, c.workoutId(), c.workoutName(), c.date(), c.duration(), c.effort(),
                    c.bodyWeight(), c.notes(), LocalDate.now(clock));
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException(e.getMessage());
        }
        s.marcarPersistencia(LocalDateTime.now(clock));
        Session salva = repositorio.salvar(s);
        eventos.sessaoRegistrada(salva.getId(), salva.getWorkoutId(), salva.getDate());
        return salva;
    }

    @Override
    public Session atualizar(String id, SessaoCommand c) {
        if (c.id() != null && !c.id().isBlank() && !c.id().equals(id)) {
            throw new RegraNegocioException("O id do corpo difere do id da URL.");
        }
        Session s = buscar(id);
        String fichaAnterior = s.getWorkoutId();
        try {
            s.alterar(c.workoutId(), c.workoutName(), c.date(), c.duration(), c.effort(),
                    c.bodyWeight(), c.notes(), LocalDate.now(clock));
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException(e.getMessage());
        }
        s.marcarPersistencia(LocalDateTime.now(clock));
        Session salva = repositorio.salvar(s);
        if (!fichaAnterior.equals(salva.getWorkoutId())) {
            eventos.sessaoExcluida(salva.getId(), fichaAnterior);
        }
        eventos.sessaoRegistrada(salva.getId(), salva.getWorkoutId(), salva.getDate());
        return salva;
    }

    @Override
    public void excluir(String id) {
        Session s = buscar(id);
        repositorio.excluir(id);
        eventos.sessaoExcluida(id, s.getWorkoutId());
    }

    @Override
    public EstatisticasProgresso estatisticas() {
        List<Session> todas = repositorio.listarTodas().stream().sorted(MAIS_RECENTE_PRIMEIRO).toList();
        LocalDate hoje = LocalDate.now(clock);
        LocalDate segunda = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<DiaSemana> semana = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate dia = segunda.plusDays(i);
            List<Session> doDia = todas.stream().filter(s -> s.getDate().equals(dia)).toList();
            semana.add(new DiaSemana(dia, DIAS.get(i), doDia.size(), doDia.stream().mapToLong(Session::getDuration).sum()));
        }

        long minutos = todas.stream().mapToLong(Session::getDuration).sum();
        long sessoesMes = todas.stream()
                .filter(s -> s.getDate().getYear() == hoje.getYear() && s.getDate().getMonth() == hoje.getMonth())
                .count();
        Double esforco = todas.isEmpty() ? null : BigDecimal.valueOf(
                        todas.stream().mapToInt(Session::getEffort).average().orElse(0))
                .setScale(1, RoundingMode.HALF_UP).doubleValue();
        BigDecimal ultimoPeso = todas.stream().map(Session::getBodyWeight).filter(Objects::nonNull).findFirst().orElse(null);
        Map<String, Long> porFicha = todas.stream()
                .collect(Collectors.groupingBy(Session::getWorkoutName, LinkedHashMap::new, Collectors.counting()));

        return new EstatisticasProgresso(
                todas.size(), minutos,
                semana.stream().mapToLong(DiaSemana::count).sum(),
                semana.stream().mapToLong(DiaSemana::minutes).sum(),
                sessoesMes, esforco, ultimoPeso,
                todas.isEmpty() ? null : todas.get(0).getDate(),
                sequencia(todas, hoje), semana, porFicha);
    }

    // ------------------------------------------------- eventos do treinos-service

    @Override
    public void fichaRenomeada(String fichaId, String novoNome) {
        List<Session> sessoes = repositorio.listarPorFicha(fichaId);
        sessoes.forEach(s -> s.fichaRenomeada(novoNome));
        repositorio.salvarTodas(sessoes);
    }

    @Override
    public void fichaExcluida(String fichaId) {
        List<Session> sessoes = repositorio.listarPorFicha(fichaId);
        sessoes.forEach(Session::fichaExcluida);
        repositorio.salvarTodas(sessoes);
    }

    /** Dias consecutivos com treino terminando hoje (ou ontem, se ainda não treinou hoje). */
    private int sequencia(List<Session> todas, LocalDate hoje) {
        Set<LocalDate> datas = todas.stream().map(Session::getDate).collect(Collectors.toSet());
        LocalDate dia = datas.contains(hoje) ? hoje : hoje.minusDays(1);
        int n = 0;
        while (datas.contains(dia)) {
            n++;
            dia = dia.minusDays(1);
        }
        return n;
    }
}
