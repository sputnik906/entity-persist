package com.github.sputnik906.persist.api.repository.query;

import lombok.Getter;

@Getter
public class ScalarJpaQuery<T> extends AbstarctJpaQuery<T>{

  private final String[] selects;

  public ScalarJpaQuery(Class<T> clazz,String[] selects) {
    super(clazz);
    this.selects=selects;
  }
}
