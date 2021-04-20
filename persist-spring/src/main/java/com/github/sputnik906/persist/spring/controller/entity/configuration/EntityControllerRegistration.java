package com.github.sputnik906.persist.spring.controller.entity.configuration;

import com.github.sputnik906.persist.spring.controller.entity.StringIdEntityController;
import com.github.sputnik906.persist.spring.controller.entity.EntityController;
import com.github.sputnik906.persist.spring.controller.entity.LongIdEntityController;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.github.sputnik906.persist.api.repository.EntityRepository;

@Configuration
@Lazy(false)
public class EntityControllerRegistration {

  protected static final Set<String> entityBasePackages = new HashSet<>();
  protected static final Set<Class<?>> excludeEntityClasses = new HashSet<>();

  @Autowired
  private EntityManager em;

  @Autowired
  private EntityRepository entityRepository;

  @Value("${entity-controller.url:null}")
  private String entityControllerUrlTemplate;

  @Autowired
  public void setHandlerMapping(RequestMappingHandlerMapping mapping){

   for(EntityType<?> entityType:em.getMetamodel().getEntities()){

      if (!Serializable.class.isAssignableFrom(entityType.getIdType().getJavaType())) continue;

      if (excludeEntityClasses.contains(entityType.getJavaType())) continue;

     LongIdEntityController<?> entityControllerHandlerLong = null;
     StringIdEntityController<?> entityControllerHandlerString = null;
     EntityController<?,? extends Serializable> entityControllerHandler = null;

     if (entityType.getIdType().getJavaType().equals(Long.class)){
       entityControllerHandlerLong = new LongIdEntityController<>(
         entityType.getJavaType(),
         entityRepository
       );
     }else if (entityType.getIdType().getJavaType().equals(String.class)){
       entityControllerHandlerString = new StringIdEntityController<>(
         entityType.getJavaType(),
         entityRepository
       );
     }else{
       entityControllerHandler = EntityController.from(
         entityType.getJavaType(),
         (Class<? extends Serializable>)entityType.getIdType().getJavaType(),
         entityRepository
       );
     }

      for (Method method : EntityController.class.getDeclaredMethods()){
        RequestMapping requestMapping = method.getDeclaredAnnotation(RequestMapping.class);
        if (requestMapping==null) continue;

        RequestMappingInfo info = RequestMappingInfo
          .paths(getRootUrl(entityType) +(requestMapping.value().length>0?requestMapping.value()[0]:""))
          .methods(requestMapping.method())
          .params(requestMapping.params())
          .consumes(requestMapping.consumes())
          .headers(requestMapping.headers())
          .produces(requestMapping.produces())
          .build();

        if (entityControllerHandlerLong!=null){
          mapping.registerMapping(info, entityControllerHandlerLong, method);
        }else if (entityControllerHandlerString!=null){
          mapping.registerMapping(info, entityControllerHandlerString, method);
        }else {
          mapping.registerMapping(info, entityControllerHandler, method);
        }

      }

    }

  }

  private String getRootUrl(EntityType<?> entityType){
     if (entityControllerUrlTemplate!=null){
       ExpressionParser parser = new SpelExpressionParser();
       StandardEvaluationContext context = new StandardEvaluationContext();
       context.setVariable("EntityClassName", entityType.getName());
       return parser.parseExpression(entityControllerUrlTemplate).getValue(context,String.class);
     }
     return "/api/"+entityType.getName().toLowerCase()+"s";
  }

}
