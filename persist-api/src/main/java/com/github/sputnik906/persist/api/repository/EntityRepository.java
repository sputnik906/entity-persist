package com.github.sputnik906.persist.api.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;


public interface EntityRepository extends QueryEntityRepository {
  <T> T persist(T entity);

  <T> T merge(T entity);

  <T> Optional<T> patch(Serializable id, Long version, Class<T> clazz,Map<String, Object> patch);

  <T> List<T> persistAll(Iterable<T> entities);

  <T> List<T> mergeAll(Iterable<T> entities);

  void flush();

  <T> void deleteInBatch(Iterable<T> entities);

  <T> void deleteAllInBatch(Class<T> clazz);

  <T> void deleteById(Serializable id,Class<T> clazz);

  <T> void delete(T entity);

  <T> void deleteAll(Iterable<T> entities);

  <T> void deleteAll(Class<T> clazz);

  <T> void deleteAllById(Iterable<Serializable> ids, Class<T> clazz);

  void inTransaction(Consumer<EntityRepository> consumer);

  <R> R inTransactionWithResult(Function<EntityRepository,R> function);
}
