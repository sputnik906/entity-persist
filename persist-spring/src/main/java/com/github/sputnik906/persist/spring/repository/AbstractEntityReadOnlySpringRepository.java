package com.github.sputnik906.persist.spring.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import com.github.sputnik906.persist.api.repository.typed.ReadOnlyRepository;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AbstractEntityReadOnlySpringRepository<ID,DOMAIN> implements
    ReadOnlyRepository<ID,DOMAIN> {

  protected final JpaEntityRepository<ID,DOMAIN> repository;
  protected final EntityManager entityManager;
  protected final Class<DOMAIN> domainClass;

  @Override
  public Optional<DOMAIN> findById(ID id) {
    return repository.findById(id);
  }

  @Override
  public Optional<DOMAIN> findById(ID id, LockModeType lockModeType) {
    return Optional.of(entityManager.find(domainClass,id,lockModeType));
  }

  @Override
  public <DTO> Optional<DTO> findById(ID id, Class<DTO> dto) {
    return repository.findById(id,dto);
  }

  @Override
  public List<DOMAIN> findAllById(Iterable<ID> ids) {
    return repository.findAllById(ids);
  }

  @Override
  public List<DOMAIN> findAll() {
    return repository.findAll();
  }

  @Override
  public <DTO> List<DTO> findAll(Class<DTO> dto) {
    return new ArrayList<>(repository.findAllBy(dto));
  }

  @Override
  public DOMAIN getReference(ID id) {
    return entityManager.getReference(domainClass,id);
  }
}
