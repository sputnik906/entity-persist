package com.github.sputnik906.persist.api.repository.query;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.LockModeType;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public abstract class AbstarctJpaQuery<T> {
  @NonNull
  private final Class<T> clazz;

  private String jpql;

  private boolean distinct;
  private String where;
  private String orderBy;
  private String groupBy;
  private String having;
  private Integer skip;
  private Integer size;

  private LockModeType lockModeType;

  private Object[] queryParams;

  private final Map<String,Object> queryNamedParams = new HashMap<>();
  private final Map<String,Object> hints = new HashMap<>();


}


