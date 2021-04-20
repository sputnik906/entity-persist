package com.github.sputnik906.persist.spring.repository;

import com.github.sputnik906.lang.utils.BeanUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import org.springframework.transaction.annotation.Transactional;
import com.github.sputnik906.persist.api.repository.typed.MutableRepository;

@Transactional
public class AbstractEntityMutableSpringRepository<ID,DOMAIN> extends AbstractEntityReadOnlySpringRepository<ID,DOMAIN> implements
    MutableRepository<ID,DOMAIN> {

  public AbstractEntityMutableSpringRepository(
      JpaEntityRepository<ID, DOMAIN> repository, EntityManager entityManager,
      Class<DOMAIN> domainClass) {
    super(repository, entityManager, domainClass);
  }

  @Override
  public DOMAIN save(DOMAIN entity) {
    return repository.save(entity);
  }

  @Override
  public List<DOMAIN> saveAll(Iterable<DOMAIN> entities) {
    return repository.saveAll(entities);
  }

  @Override
  public void delete(DOMAIN entity) {
    repository.delete(entity);
  }

  @Override
  public void deleteById(ID id) {
    repository.deleteById(id);
  }

  @Override
  public void deleteAll(Iterable<DOMAIN> entities) {
    repository.deleteAll(entities);
  }

  @Override
  public void deleteAll() {
    repository.deleteAll();
  }

  @Override
  public void deleteAllById(Iterable<ID> ids) {
    for (ID id : ids) {
      deleteById(id);
    }
  }

  @Override
  public Optional<DOMAIN> patch(ID id, Long version, Map<String, Object> patch) {
    EntityType<DOMAIN> entityType = entityManager.getMetamodel().entity(domainClass);

    String idName = entityType.getId(entityType.getIdType().getJavaType()).getName();

    String versionName = entityManager.getMetamodel().entity(domainClass).getVersion(Long.class).getName();

    patch.remove(idName);
    patch.remove(versionName);
    return findById(id,domainClass).map(e->{
      if (version!=null&&!version.equals(BeanUtils.getNestedProperty(e,versionName)))
        throw new IllegalArgumentException("Wrong version");
      BeanUtils.setProperties(e,patch);
      return e;
    });
  }

  @Override
  public Optional<DOMAIN> patch(ID id, Long version, Consumer<DOMAIN> consumer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void inTransaction(Runnable runnable) {
    runnable.run();
  }

  @Override
  public <R> R inTransactionWithResult(Supplier<R> supplier) {
    return supplier.get();
  }
}
