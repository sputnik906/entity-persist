package com.github.sputnik906.persist.jpa.repository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.github.sputnik906.persist.api.repository.EntityRepository;
import com.github.sputnik906.persist.api.repository.QueryEntityRepository;
import com.github.sputnik906.persist.api.transaction.EntityTransactionManager;
import com.github.sputnik906.persist.api.transaction.TransactionParams;

@RequiredArgsConstructor
class EntityRepositoryTransactionHandler implements InvocationHandler {

  @NonNull private final EntityRepository original;
  @NonNull private final EntityTransactionManager transactionManager;

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    TransactionParams transactionParams = new TransactionParams();
    if (method.getDeclaringClass().equals(QueryEntityRepository.class)) transactionParams.setReadOnly(true);
    return transactionManager.withResult(status-> {
      try {
        return method.invoke(original, args);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new IllegalStateException(e);
      }
    },transactionParams);
  }
}
