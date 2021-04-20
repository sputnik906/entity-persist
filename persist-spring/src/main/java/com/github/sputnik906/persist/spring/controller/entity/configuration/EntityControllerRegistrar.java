package com.github.sputnik906.persist.spring.controller.entity.configuration;

import java.util.Arrays;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;


public class EntityControllerRegistrar implements ImportBeanDefinitionRegistrar {
  @Override
  public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
    BeanNameGenerator generator) {

    EnableAutoEntityControllers a = metadata.getAnnotations()
      .get(EnableAutoEntityControllers.class).synthesize();

    EntityControllerRegistration.entityBasePackages.addAll(Arrays.asList(a.basePackages()));
    EntityControllerRegistration.excludeEntityClasses.addAll(Arrays.asList(a.excludeEntityClasses()));

  }
}
