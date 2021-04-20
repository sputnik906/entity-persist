package com.github.sputnik906.persist.spring;

import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.github.sputnik906.persist.api.transaction.EntityTransactionManager;
import com.github.sputnik906.persist.api.transaction.TransactionContext;
import com.github.sputnik906.persist.api.transaction.TransactionParams;

@RequiredArgsConstructor
public class EntityTransactionManagerAdapter implements EntityTransactionManager {

  private final PlatformTransactionManager transactionManager;

  @Override
  public void withoutResult(
    Consumer<TransactionContext> callback,
    TransactionParams params) {

    createTransactionTemplate(params)
      .executeWithoutResult(status->callback.accept(new TransactionContext() {
        })
      );
  }

  @Override
  public <R> R withResult(
    Function<TransactionContext, R> callback,
    TransactionParams params) {

    return createTransactionTemplate(params)
      .execute(status -> callback.apply(new TransactionContext() {
    }));

  }

  private TransactionTemplate createTransactionTemplate(TransactionParams params){
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setReadOnly(params.isReadOnly());
    return transactionTemplate;
  }
}
