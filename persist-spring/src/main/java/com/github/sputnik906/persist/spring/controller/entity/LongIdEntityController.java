package com.github.sputnik906.persist.spring.controller.entity;


import com.github.sputnik906.persist.api.repository.EntityRepository;

public class LongIdEntityController<T> extends EntityController<T,Long> {

  public LongIdEntityController(Class<T> domainClass,
    EntityRepository repository) {
    super(domainClass, Long.class, repository);
  }
}
