package com.github.sputnik906.persist.api.repository.typed;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface MutableRepository<ID, DOMAIN> extends ReadOnlyRepository<ID, DOMAIN> {

  DOMAIN save(DOMAIN entity);

  List<DOMAIN> saveAll(Iterable<DOMAIN> entities);

  void delete(DOMAIN entity);

  void deleteById(ID id);

  void deleteAll(Iterable<DOMAIN> entities);

  void deleteAll();

  void deleteAllById(Iterable<ID> ids);

  Optional<DOMAIN> patch(ID id, Long version, Map<String, Object> patch);

  Optional<DOMAIN> patch(ID id, Long version, Consumer<DOMAIN> consumer);

  default Optional<DOMAIN> patch(ID id, Map<String, Object> patch) {
    return patch(id, null, patch);
  }

  default Optional<DOMAIN> patch(ID id, Consumer<DOMAIN> consumer) {
    return patch(id, null, consumer);
  }

  void inTransaction(Runnable runnable);

  <R> R inTransactionWithResult(Supplier<R> supplier);
}
