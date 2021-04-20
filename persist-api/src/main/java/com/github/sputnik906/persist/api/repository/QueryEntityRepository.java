package com.github.sputnik906.persist.api.repository;


import com.github.sputnik906.persist.api.repository.query.WhereBuilder;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.LockModeType;
import com.github.sputnik906.persist.api.repository.query.EntityJpaQuery;
import com.github.sputnik906.persist.api.repository.query.ScalarJpaQuery;

public interface QueryEntityRepository {

  String[] AGGREGATE_FUNCTIONS = new String[]{"max","min","count","avg","sum"};

  String ROOT_CLASS_ALIAS = "e";

  default <T> List<T> query(Class<T> entity, WhereBuilder whereBuilder,String orderBy){
    EntityJpaQuery<T> query = new EntityJpaQuery<T>(entity);
    query.setWhere(whereBuilder.toString());
    query.setQueryParams(whereBuilder.getParams().toArray());
    query.setOrderBy(orderBy);
    return query(query);
  }

  default <T> List<T> query(Class<T> entity, WhereBuilder whereBuilder){
    return query(entity,whereBuilder,null);
  }

  <T> List<T> query(EntityJpaQuery<T> query);

  <T> List<Map<String,Object>> query(EntityJpaQuery<T> params,String[] propPaths);

  <T,R> List<R> query(EntityJpaQuery<T> params,Class<R> resultType);

  <T,R> List<R> query(ScalarJpaQuery<T> params,Class<R> resultType);

  <T> List<Map<String,Object>> query(ScalarJpaQuery<T> params);

  <T> Optional<T> findById(Serializable id, Class<T> clazz, LockModeType lockModeType,Map<String,Object> hints);

  default <T> Optional<T> findById(Serializable id, Class<T> clazz, LockModeType lockModeType){
    return findById(id,clazz,lockModeType,new HashMap<>());
  }

  <T> Optional<T> findById(Serializable id, Class<T> clazz);

  <T> Optional<Map<String,Object>> findById(Serializable id, Class<T> clazz,String[] propPaths);

  <T,R> Optional<R> findById(Serializable id, Class<T> clazz,Class<R> resultType);

  <T,ID extends Serializable> List<T> findAllById(Iterable<ID> ids, Class<T> clazz);

  <T> List<Map<String,Object>> findAllById(Iterable<Serializable> ids, Class<T> clazz,String[] propPaths);

  <T,R> List<R> findAllById(Iterable<Serializable> ids, Class<T> clazz,Class<R> resultType);

  <T> T getReference(Serializable id, Class<T> clazz);

  <T> List<T> namedTypedQuery(Class<T> clazz,String name,Object ... params);

  List<?> namedQuery(Class<?> clazz,String name,Object ... params);

  Object namedQuerySingle(Class<?> clazz,String name,Object ... params);

  Class<?> getEntityClassByName(String name);

  String getIdFieldName(Class<?> entityClass);

  default <T> List<T> findAll(Class<T> clazz){
    return query(new EntityJpaQuery<>(clazz));
  }

  default Object aggregateFun(String name,String field,String where,Class<?> clazz){
    ScalarJpaQuery<?> query = new ScalarJpaQuery<>(
      clazz,
      new String[]{name+"("+ROOT_CLASS_ALIAS+(field!=null?"."+field:"")+") as "+name}
      );
    query.setWhere(where);
    return query(query).get(0).get(name);
  }

}
