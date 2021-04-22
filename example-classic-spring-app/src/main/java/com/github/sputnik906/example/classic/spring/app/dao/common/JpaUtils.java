package com.github.sputnik906.example.classic.spring.app.dao.common;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class JpaUtils {

  public <FIELD_TYPE extends Number,DOMAIN> FIELD_TYPE sum(
    EntityManager em,
    Specification<DOMAIN> spec,
    SingularAttribute<DOMAIN,FIELD_TYPE> column
  ){
    return aggregateFunWithSpecification(
      JpaAggregateFun.sum,
      em,
      spec,
      column
    );
  }

  public <FIELD_TYPE extends Number,DOMAIN> FIELD_TYPE min(
    EntityManager em,
    Specification<DOMAIN> spec,
    SingularAttribute<DOMAIN,FIELD_TYPE> column
  ){
    return aggregateFunWithSpecification(
      JpaAggregateFun.min,
      em,
      spec,
      column
    );
  }

  public <FIELD_TYPE extends Number,DOMAIN> FIELD_TYPE max(
    EntityManager em,
    Specification<DOMAIN> spec,
    SingularAttribute<DOMAIN,FIELD_TYPE> column
  ){
    return aggregateFunWithSpecification(
      JpaAggregateFun.max,
      em,
      spec,
      column
    );
  }


  private enum JpaAggregateFun{
    sum,min,max
  }

  private <FIELD_TYPE extends Number,DOMAIN> FIELD_TYPE aggregateFunWithSpecification(
    JpaAggregateFun fun,
    EntityManager em,
    Specification<DOMAIN> spec,
    SingularAttribute<DOMAIN,FIELD_TYPE> column
  ){
    Class<DOMAIN> domainClass = column.getDeclaringType().getJavaType();

    CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
    CriteriaQuery<FIELD_TYPE> query = criteriaBuilder.createQuery(column.getJavaType());
    Root<DOMAIN> root = query.from(domainClass);

    if (spec != null) query.where(spec.toPredicate(root, query, criteriaBuilder));

    switch (fun){
      case sum:query.select(criteriaBuilder.sum(root.get(column.getName())));break;
      case min:query.select(criteriaBuilder.min(root.get(column.getName())));break;
      case max:query.select(criteriaBuilder.max(root.get(column.getName())));break;
    }

    TypedQuery<FIELD_TYPE> typedQuery = em.createQuery(query);

    return typedQuery.getSingleResult();
  }

  public <DOMAIN> Double avg(
    EntityManager em,
    Specification<DOMAIN> spec,
    SingularAttribute<DOMAIN,Double> column
  ){
    Class<DOMAIN> domainClass = column.getDeclaringType().getJavaType();

    CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
    CriteriaQuery<Double> query = criteriaBuilder.createQuery(column.getJavaType());
    Root<DOMAIN> root = query.from(domainClass);

    if (spec != null) query.where(spec.toPredicate(root, query, criteriaBuilder));

    query.select(criteriaBuilder.avg(root.get(column.getName())));

    TypedQuery<Double> typedQuery = em.createQuery(query);

    return typedQuery.getSingleResult();
  }


}
