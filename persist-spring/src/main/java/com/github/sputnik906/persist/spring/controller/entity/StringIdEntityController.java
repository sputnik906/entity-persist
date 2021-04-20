package com.github.sputnik906.persist.spring.controller.entity;


import com.github.sputnik906.persist.api.repository.EntityRepository;

public class StringIdEntityController<T> extends EntityController<T,String>{

  public StringIdEntityController(Class<T> domainClass,
    EntityRepository repository) {
    super(domainClass, String.class, repository);
  }
}
