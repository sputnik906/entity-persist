package com.github.sputnik906.example.classic.spring.app;

import com.github.sputnik906.example.classic.spring.app.domain.DataSet;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@Profile("dev")
public class LoadDevDataSet implements ApplicationRunner {

  @Autowired private EntityManager entityManager;

  @Autowired private PlatformTransactionManager transactionManager;

  @Override
  public void run(ApplicationArguments args) {

    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

    transactionTemplate.executeWithoutResult(
        s -> {
          DataSet.getDev().forEach((k, v) -> v.forEach(e -> entityManager.persist(e)));
        });
  }
}
