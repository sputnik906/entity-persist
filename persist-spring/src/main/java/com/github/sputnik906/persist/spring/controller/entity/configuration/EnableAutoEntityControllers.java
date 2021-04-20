package com.github.sputnik906.persist.spring.controller.entity.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({
  EntityControllerRegistrar.class,
  EntityControllerRegistration.class
})
public @interface EnableAutoEntityControllers {

  String[] basePackages() default {};

  Class<?>[] excludeEntityClasses() default {};
}
