package com.github.sputnik906.persist.spring.controller.mapper;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleEntityJacksonConfiguration {
  @Autowired
  private EntityManager em;

  //https://mostafa-asg.github.io/post/customize-json-xml-spring-mvc-output/
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer entityJacksonObjectMapper(){
    return builder -> {
      em.getMetamodel().getEntities()
        .forEach(e->builder.mixIn(e.getJavaType(),EntityMixin.class) );
    };
  }

  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  public static class EntityMixin{ }
}
