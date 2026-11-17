package br.fitforge.treinos.application;

import br.fitforge.treinos.application.port.in.FichaCommand;
import br.fitforge.treinos.application.port.out.FichaEventPublisherPort;
import br.fitforge.treinos.application.port.out.FichaRepositoryPort;
import br.fitforge.treinos.application.service.FichaService;
import br.fitforge.treinos.domain.exception.ConflitoException;
import br.fitforge.treinos.domain.exception.RecursoNaoEncontradoException;
import br.fitforge.treinos.domain.exception.RegraNegocioException;
import br.fitforge.treinos.domain.model.Exercise;
import br.fitforge.treinos.domain.model.Workout;
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

/** Testes unitários do núcleo (sem Spring, sem MongoDB, sem Service Bus): portas falsas em memória. */
class FichaServiceTest {

    static class RepositorioEmMemoria implements FichaRepositoryPort {
        final Map<String, Workout> dados = new LinkedHashMap<>();
        public List<Workout> listarTodas() { return new ArrayList<>(dados.values()); }
        public Optional<Workout> buscarPorId(String id) { return Optional.ofNullable(dados.get(id)); }
        public boolean existe(String id) { return dados.containsKey(id); }
        public Workout salvar(Workout f) { dados.put(f.getId(), f); return f; }
        public void excluir(String id) { dados.remove(id); }
        public long contar() { return dados.size(); }
    }

    static class EventosCapturados implements FichaEventPublisherPort {
        final List<String> eventos = new ArrayList<>();
        public void fichaRenomeada(String id, String nome) { eventos.add("RENOMEADA:" + id + ":" + nome); }
        public void fichaExcluida(String id, String nome) { eventos.add("EXCLUIDA:" + id); }
    }

    RepositorioEmMemoria repo;
    EventosCapturados eventos;
    FichaService service;

    @BeforeEach
    void setUp() {
        repo = new RepositorioEmMemoria();
        eventos = new EventosCapturados();
        service = new FichaService(repo, eventos, Clock.fixed(Instant.parse("2026-11-17T12:00:00Z"), ZoneId.of("UTC")));
    }

    private static FichaCommand comando(String id, String nome, String... dias) {
        return new FichaCommand(id, nome, "desc", List.of(dias),
                List.of(new Exercise("e1", "Supino", "Peito", 3, 10, new BigDecimal("20"), 60)));
    }

    @Test
    void criaFichaOrdenandoOsDiasDaSemana() {
        Workout w = service.criar(comando("f1", "Treino X", "Sex", "Seg"));
        assertThat(w.getDays()).containsExactly("Seg", "Sex");
        assertThat(w.getCreatedAt()).isNotNull();
        assertThat(w.volume()).isEqualByComparingTo("600");
    }

    @Test
    void naoPermiteIdDuplicado() {
        service.criar(comando("f1", "Treino X", "Seg"));
        assertThatThrownBy(() -> service.criar(comando("f1", "Outro", "Ter"))).isInstanceOf(ConflitoException.class);
    }

    @Test
    void rejeitaDiaInvalidoOuRepetido() {
        assertThatThrownBy(() -> service.criar(comando("f1", "Treino", "Seg", "Seg"))).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> service.criar(comando("f2", "Treino", "Domingo"))).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void renomearPublicaEventoNoBroker() {
        service.criar(comando("f1", "Treino X", "Seg"));
        service.atualizar("f1", comando("f1", "Treino Y", "Seg"));
        assertThat(eventos.eventos).containsExactly("RENOMEADA:f1:Treino Y");
    }

    @Test
    void atualizarSemMudarNomeNaoPublicaEvento() {
        service.criar(comando("f1", "Treino X", "Seg"));
        service.atualizar("f1", comando("f1", "Treino X", "Seg", "Qua"));
        assertThat(eventos.eventos).isEmpty();
    }

    @Test
    void excluirPublicaEventoEApagaDoRepositorio() {
        service.criar(comando("f1", "Treino X", "Seg"));
        service.excluir("f1");
        assertThat(repo.dados).isEmpty();
        assertThat(eventos.eventos).containsExactly("EXCLUIDA:f1");
    }

    @Test
    void excluirFichaInexistenteDa404() {
        assertThatThrownBy(() -> service.excluir("nao-existe")).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void eventoDeSessaoAtualizaContadorDeFormaIdempotente() {
        service.criar(comando("f1", "Treino X", "Seg"));
        service.sessaoRegistrada("s1", "f1", LocalDate.of(2026, 11, 10));
        service.sessaoRegistrada("s1", "f1", LocalDate.of(2026, 11, 10)); // mensagem duplicada
        service.sessaoRegistrada("s2", "f1", LocalDate.of(2026, 11, 15));
        Workout w = service.buscar("f1");
        assertThat(w.totalSessoes()).isEqualTo(2);
        assertThat(w.ultimaSessao()).isEqualTo(LocalDate.of(2026, 11, 15));

        service.sessaoExcluida("s2", "f1");
        assertThat(service.buscar("f1").totalSessoes()).isEqualTo(1);
        assertThat(service.buscar("f1").ultimaSessao()).isEqualTo(LocalDate.of(2026, 11, 10));
    }

    @Test
    void eventoDeFichaInexistenteEhIgnorado() {
        service.sessaoRegistrada("s1", "nao-existe", LocalDate.now());
        assertThat(repo.dados).isEmpty();
    }

    @Test
    void estatisticasSomamSessoesEExercicios() {
        service.criar(comando("f1", "Treino X", "Seg"));
        service.sessaoRegistrada("s1", "f1", LocalDate.of(2026, 11, 10));
        var stats = service.estatisticas();
        assertThat(stats.totalFichas()).isEqualTo(1);
        assertThat(stats.totalExercicios()).isEqualTo(1);
        assertThat(stats.totalSessoesRealizadas()).isEqualTo(1);
        assertThat(stats.fichasPorDia().get("Seg")).isEqualTo(1);
    }
}
