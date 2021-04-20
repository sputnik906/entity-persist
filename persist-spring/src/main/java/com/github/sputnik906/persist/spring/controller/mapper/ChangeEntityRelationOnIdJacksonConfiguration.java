package com.github.sputnik906.persist.spring.controller.mapper;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.github.sputnik906.entity.jackson.ChangeEntityRelationOnIdFilter;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChangeEntityRelationOnIdJacksonConfiguration {
  @Autowired
  private EntityManager em;

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer entityJacksonObjectMapper(){
    return builder -> {

      Set<Class<?>> allEntities =  em.getMetamodel().getEntities().stream()
        .map(Type::getJavaType)
        .collect(Collectors.toSet());

      allEntities.forEach(e->builder.mixIn(e, EntityMixin.class));

      Set<Class<?>> allEmbeddables = em.getMetamodel().getEmbeddables().stream()
        .map(Type::getJavaType)
        .collect(Collectors.toSet());

      allEmbeddables.forEach(e->builder.mixIn(e, EntityMixin.class));

      SimpleFilterProvider filterProvider = new SimpleFilterProvider();
      filterProvider.addFilter(
        ChangeEntityRelationOnIdFilter.ID,
        new ChangeEntityRelationOnIdFilter(allEntities,true)
      );

      builder.filters(filterProvider);
    };
  }

  @JsonFilter(ChangeEntityRelationOnIdFilter.ID)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  public static class EntityMixin{ }
}
