package com.github.sputnik906.persist.jpa.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import com.github.sputnik906.lang.utils.BeanUtils;
import com.github.sputnik906.persist.api.repository.EntityRepository;
import com.github.sputnik906.persist.api.repository.query.EntityJpaQuery;


class EntityRepositoryImpl extends QueryEntityRepositoryImpl implements
  EntityRepository {

  public EntityRepositoryImpl(EntityManager em) {
    super(em);
  }

  @Override
  public <T> T persist(T entity) {
    em.persist(entity);
    return entity;
  }

  @Override
  public <T> T merge(T entity) {
    return em.merge(entity);
  }

  @Override
  public <T> Optional<T> patch(Serializable id, Long version,Class<T> clazz,
    Map<String, Object> patch) {

    EntityType<T> entityType = em.getMetamodel().entity(clazz);

    String idName = entityType.getId(entityType.getIdType().getJavaType()).getName();

    String versionName = em.getMetamodel().entity(clazz).getVersion(Long.class).getName();

    patch.remove(idName);
    patch.remove(versionName);
    return findById(id,clazz).map(e->{
      if (version!=null&&!version.equals(BeanUtils.getNestedProperty(e,versionName)))
        throw new IllegalArgumentException("Wrong version");
      BeanUtils.setProperties(e,patch);
      return e;
    });

  }


  @Override
  public <T> List<T> persistAll(Iterable<T> entities) {
    List<T> result = new ArrayList<>();

    for (T entity : entities) {
      result.add(persist(entity));
    }

    return result;
  }

  @Override
  public <T> List<T> mergeAll(Iterable<T> entities) {
    List<T> result = new ArrayList<>();

    for (T entity : entities) {
      result.add(merge(entity));
    }

    return result;
  }

  @Override
  public void flush() {
    em.flush();
  }

  @Override
  public <T> void deleteInBatch(Iterable<T> entities) {
    throw  new UnsupportedOperationException();
  }

  @Override
  public <T> void deleteAllInBatch(Class<T> clazz) {
    throw  new UnsupportedOperationException();
  }

  @Override
  public <T> void deleteById(Serializable id,Class<T> clazz) {
    delete(findById(id,clazz));
  }

  @Override
  public <T> void delete(T entity) {
    em.remove(em.contains(entity) ? entity : em.merge(entity));
  }

  @Override
  public <T> void deleteAll(Iterable<T> entities) {
    for (T entity : entities) {
      delete(entity);
    }
  }

  @Override
  public <T> void deleteAll(Class<T> clazz) {
    for (T element : query(new EntityJpaQuery<>(clazz))) {
      delete(element);
    }
  }

  @Override
  public <T> void deleteAllById(Iterable<Serializable> ids, Class<T> clazz) {
    for (Serializable id : ids) {
      deleteById(id,clazz);
    }
  }

  @Override
  public void inTransaction(Consumer<EntityRepository> consumer) {
    consumer.accept(this);
  }

  @Override
  public <R> R inTransactionWithResult(Function<EntityRepository, R> function) {
    return function.apply(this);
  }
}
