package br.fitforge.progresso.adapter.out.persistence;

import br.fitforge.progresso.application.port.out.SessaoRepositoryPort;
import br.fitforge.progresso.domain.model.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Adaptador de saída: implementa a porta de persistência com JPA (Azure SQL Database em produção). */
@Component
@Transactional
public class JpaSessaoRepositoryAdapter implements SessaoRepositoryPort {

    private final SpringDataSessionRepository repository;

    JpaSessaoRepositoryAdapter(SpringDataSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> listarTodas() {
        return repository.findAll().stream().map(JpaSessaoRepositoryAdapter::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> listarPorFicha(String workoutId) {
        return repository.findByWorkoutId(workoutId).stream().map(JpaSessaoRepositoryAdapter::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Session> buscarPorId(String id) {
        return repository.findById(id).map(JpaSessaoRepositoryAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existe(String id) {
        return repository.existsById(id);
    }

    @Override
    public Session salvar(Session sessao) {
        return toDomain(repository.save(toEntity(sessao)));
    }

    @Override
    public void salvarTodas(List<Session> sessoes) {
        repository.saveAll(sessoes.stream().map(JpaSessaoRepositoryAdapter::toEntity).toList());
    }

    @Override
    public void excluir(String id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long contar() {
        return repository.count();
    }

    static SessionEntity toEntity(Session s) {
        SessionEntity e = new SessionEntity();
        e.setId(s.getId());
        e.setWorkoutId(s.getWorkoutId());
        e.setWorkoutName(s.getWorkoutName());
        e.setDate(s.getDate());
        e.setDuration(s.getDuration());
        e.setEffort(s.getEffort());
        e.setBodyWeight(s.getBodyWeight());
        e.setNotes(s.getNotes());
        e.setFichaAtiva(s.isFichaAtiva());
        e.setCreatedAt(s.getCreatedAt());
        e.setUpdatedAt(s.getUpdatedAt());
        return e;
    }

    static Session toDomain(SessionEntity e) {
        return Session.reconstituir(e.getId(), e.getWorkoutId(), e.getWorkoutName(), e.getDate(), e.getDuration(),
                e.getEffort(), e.getBodyWeight(), e.getNotes(), e.isFichaAtiva(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
