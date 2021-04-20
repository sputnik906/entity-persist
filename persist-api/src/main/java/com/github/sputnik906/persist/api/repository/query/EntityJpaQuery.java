package com.github.sputnik906.persist.api.repository.query;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class EntityJpaQuery<T> extends AbstarctJpaQuery<T> {

  private String[] fetchProps;

  public EntityJpaQuery(Class<T> clazz) {
    super(clazz);
  }
}
