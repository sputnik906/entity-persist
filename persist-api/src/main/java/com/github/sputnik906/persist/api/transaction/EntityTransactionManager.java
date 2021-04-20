package com.github.sputnik906.persist.api.transaction;

import java.util.function.Consumer;
import java.util.function.Function;

public interface EntityTransactionManager {

  void withoutResult(Consumer<TransactionContext> callback,TransactionParams params);

  <R> R withResult(Function<TransactionContext,R> callback,TransactionParams params);

  default void withoutResult(Consumer<TransactionContext> callback){
    withoutResult(callback,null);
  }

  default <R> R withResult(Function<TransactionContext,R> callback){
    return withResult(callback,null);
  }
}
