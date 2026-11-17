package br.fitforge.progresso.application;

import br.fitforge.progresso.application.port.in.SessaoCommand;
import br.fitforge.progresso.application.port.out.SessaoEventPublisherPort;
import br.fitforge.progresso.application.port.out.SessaoRepositoryPort;
import br.fitforge.progresso.application.service.SessaoService;
import br.fitforge.progresso.domain.exception.ConflitoException;
import br.fitforge.progresso.domain.exception.RegraNegocioException;
import br.fitforge.progresso.domain.model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testes unitários do núcleo do progresso (sem Spring, sem banco, sem Service Bus). */
class SessaoServiceTest {

    static class RepositorioEmMemoria implements SessaoRepositoryPort {
        final Map<String, Session> dados = new LinkedHashMap<>();
        public List<Session> listarTodas() { return new ArrayList<>(dados.values()); }
        public List<Session> listarPorFicha(String w) { return dados.values().stream().filter(s -> s.getWorkoutId().equals(w)).toList(); }
        public Optional<Session> buscarPorId(String id) { return Optional.ofNullable(dados.get(id)); }
        public boolean existe(String id) { return dados.containsKey(id); }
        public Session salvar(Session s) { dados.put(s.getId(), s); return s; }
        public void salvarTodas(List<Session> l) { l.forEach(this::salvar); }
        public void excluir(String id) { dados.remove(id); }
        public long contar() { return dados.size(); }
    }

    static class EventosCapturados implements SessaoEventPublisherPort {
        final List<String> eventos = new ArrayList<>();
        public void sessaoRegistrada(String s, String f, LocalDate d) { eventos.add("REGISTRADA:" + s + ":" + f); }
        public void sessaoExcluida(String s, String f) { eventos.add("EXCLUIDA:" + s + ":" + f); }
    }

    static final LocalDate HOJE = LocalDate.of(2026, 11, 18); // quarta-feira

    RepositorioEmMemoria repo;
    EventosCapturados eventos;
    SessaoService service;

    @BeforeEach
    void setUp() {
        repo = new RepositorioEmMemoria();
        eventos = new EventosCapturados();
        service = new SessaoService(repo, eventos, Clock.fixed(Instant.parse("2026-11-18T15:00:00Z"), ZoneId.of("America/Sao_Paulo")));
    }

    private static SessaoCommand cmd(String id, String ficha, LocalDate data, int min) {
        return new SessaoCommand(id, ficha, "Treino " + ficha, data, min, 7, new BigDecimal("80.0"), "");
    }

    @Test
    void registrarPublicaEventoNoBroker() {
        service.criar(cmd("s1", "f1", HOJE, 40));
        assertThat(repo.dados).containsKey("s1");
        assertThat(eventos.eventos).containsExactly("REGISTRADA:s1:f1");
    }

    @Test
    void naoAceitaDataFuturaNemIdDuplicado() {
        assertThatThrownBy(() -> service.criar(cmd("s1", "f1", HOJE.plusDays(1), 40))).isInstanceOf(RegraNegocioException.class);
        service.criar(cmd("s1", "f1", HOJE, 40));
        assertThatThrownBy(() -> service.criar(cmd("s1", "f1", HOJE, 40))).isInstanceOf(ConflitoException.class);
    }

    @Test
    void trocarDeFichaPublicaExclusaoNaAntigaERegistroNaNova() {
        service.criar(cmd("s1", "f1", HOJE, 40));
        eventos.eventos.clear();
        service.atualizar("s1", cmd("s1", "f2", HOJE, 40));
        assertThat(eventos.eventos).containsExactly("EXCLUIDA:s1:f1", "REGISTRADA:s1:f2");
    }

    @Test
    void excluirPublicaEvento() {
        service.criar(cmd("s1", "f1", HOJE, 40));
        service.excluir("s1");
        assertThat(repo.dados).isEmpty();
        assertThat(eventos.eventos).contains("EXCLUIDA:s1:f1");
    }

    @Test
    void eventoDeFichaRenomeadaAtualizaSnapshotDoNome() {
        service.criar(cmd("s1", "f1", HOJE, 40));
        service.criar(cmd("s2", "f2", HOJE, 40));
        service.fichaRenomeada("f1", "Novo nome");
        assertThat(repo.dados.get("s1").getWorkoutName()).isEqualTo("Novo nome");
        assertThat(repo.dados.get("s2").getWorkoutName()).isEqualTo("Treino f2");
    }

    @Test
    void eventoDeFichaExcluidaMantemHistoricoMarcado() {
        service.criar(cmd("s1", "f1", HOJE, 40));
        service.fichaExcluida("f1");
        assertThat(repo.dados.get("s1").isFichaAtiva()).isFalse();
    }

    @Test
    void estatisticasDaSemanaESequencia() {
        service.criar(cmd("s1", "f1", HOJE, 40));
        service.criar(cmd("s2", "f1", HOJE.minusDays(1), 30));
        service.criar(cmd("s3", "f2", HOJE.minusDays(10), 50));
        var stats = service.estatisticas();
        assertThat(stats.totalSessoes()).isEqualTo(3);
        assertThat(stats.minutosTotais()).isEqualTo(120);
        assertThat(stats.sessoesNaSemana()).isEqualTo(2);
        assertThat(stats.sequenciaDiasSeguidos()).isEqualTo(2);
        assertThat(stats.semana()).hasSize(7);
        assertThat(stats.semana().get(0).label()).isEqualTo("Seg");
    }

    @Test
    void filtroPorPeriodoSemana() {
        service.criar(cmd("s1", "f1", HOJE, 40));
        service.criar(cmd("s2", "f1", HOJE.minusDays(10), 30));
        assertThat(service.listar(null, "semana")).extracting(Session::getId).containsExactly("s1");
        assertThatThrownBy(() -> service.listar(null, "ano")).isInstanceOf(RegraNegocioException.class);
    }
}
