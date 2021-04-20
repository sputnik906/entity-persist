package com.github.sputnik906.persist.jpa.repository;

import java.lang.reflect.Proxy;
import javax.persistence.EntityManager;
import lombok.experimental.UtilityClass;
import com.github.sputnik906.persist.api.repository.EntityRepository;
import com.github.sputnik906.persist.api.transaction.EntityTransactionManager;

@UtilityClass
public class EntityRepositoryFactory {
  public static EntityRepository withoutTransaction(EntityManager em){
    return new EntityRepositoryImpl(em);
  }
  public static EntityRepository withTransactionManager(EntityManager em, EntityTransactionManager transactionManager){
    EntityRepository entityRepository = withoutTransaction(em);
    return (EntityRepository) Proxy.newProxyInstance(
        EntityRepository.class.getClassLoader(),
      new Class[] { EntityRepository.class },
      new EntityRepositoryTransactionHandler(entityRepository,transactionManager));
  }
}
