package com.github.sputnik906.persist.jpa.repository;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Type;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.github.sputnik906.lang.utils.BeanMapper;
import com.github.sputnik906.persist.api.repository.QueryEntityRepository;
import com.github.sputnik906.persist.api.repository.query.AbstarctJpaQuery;
import com.github.sputnik906.persist.api.repository.query.EntityJpaQuery;
import com.github.sputnik906.persist.api.repository.query.ScalarJpaQuery;

@RequiredArgsConstructor
class QueryEntityRepositoryImpl implements QueryEntityRepository {

  @NonNull
  protected final EntityManager em;

  @Override
  public <T> List<T> query(EntityJpaQuery<T> params){
    EntityType<T> entityType = em.getMetamodel().entity(params.getClazz());

    TypedQuery<T> query = createQuery(params,
      params.getClazz(),
      ROOT_CLASS_ALIAS,
      entityType.getName()
    );

    if (params.getFetchProps()!=null)
      applyFetchGraph(params.getClazz(),entityType.getName(),query,params.getFetchProps());

    return query.getResultList();
  }

  @Override
  public <T> List<Map<String,Object>> query(EntityJpaQuery<T> params,String[] propPaths){
    List<T> entityResultList =  query(params);

    return entityResultList.stream()
      .map(checkException(e-> BeanMapper.convert(e,params.getClazz(),propPaths)))
      .collect(Collectors.toList());
  }

  @Override
  public <T, R> List<R> query(EntityJpaQuery<T> params, Class<R> resultType) {
    List<T> entityResultList =  query(params);

    return entityResultList.stream()
      .map(e->entityMapper(e,params.getClazz(),resultType))
      .collect(Collectors.toList());
  }

  @Override
  public <T,R> List<R> query(ScalarJpaQuery<T> params,Class<R> resultType){

    Constructor<R> constructor =  Stream.of(resultType.getConstructors())
      .filter(c->c.getParameterCount()==params.getSelects().length)
      .map(checkException(c->resultType.getConstructor(c.getParameterTypes())))
      .findFirst()
      .orElseThrow(IllegalStateException::new);

    return queryScalarTuple(params).stream()
      .map(checkException(t->constructor.newInstance(t.toArray())))
      .collect(Collectors.toList());
  }

  @Override
  public <T> List<Map<String,Object>> query(ScalarJpaQuery<T> params){
    List<Tuple> resultTuple = queryScalarTuple(params);
    List<Map<String,Object>> result = new ArrayList<>();
    for(Tuple tuple:resultTuple){
      Map<String,Object> resultMap = new HashMap<>();
      for(int i=0; i<tuple.getElements().size(); i++) resultMap.put(
        tuple.getElements().get(i).getAlias()!=null
          ?tuple.getElements().get(i).getAlias()
          :params.getSelects()[i],
        tuple.get(i)
      );
      result.add(resultMap);
    }
    return result;
  }

  @Override
  public <T> Optional<T> findById(Serializable id, Class<T> clazz, LockModeType lockModeType,
    Map<String, Object> hints) {
    return Optional.ofNullable(em.find(clazz,id,lockModeType,hints));
  }

  @Override
  public <T> Optional<T> findById(Serializable id, Class<T> clazz) {
    return Optional.ofNullable(em.find(clazz,id));
  }

  @Override
  public <T> Optional<Map<String, Object>> findById(Serializable id, Class<T> clazz,
    String[] propPaths) {
    return findById(id,clazz)
      .map(checkException(e-> BeanMapper.convert(e,clazz,propPaths)));
  }

  @Override
  public <T, R> Optional<R> findById(Serializable id, Class<T> clazz, Class<R> resultType) {
    return findById(id,clazz)
      .map(e->entityMapper(e,clazz,resultType));
  }

  @Override
  public <T,ID extends Serializable> List<T> findAllById(Iterable<ID> ids, Class<T> clazz) {
    //https://thoughts-on-java.org/fetch-multiple-entities-id-hibernate/
    String entityName = em.getMetamodel().entity(clazz).getName();
    return em.createQuery("SELECT p FROM "+entityName+" p WHERE p.id IN :ids",clazz)
      .setParameter("ids", ids).getResultList();
  }

  @Override
  public <T> List<Map<String, Object>> findAllById(Iterable<Serializable> ids, Class<T> clazz,
    String[] propPaths) {
    return findAllById(ids,clazz).stream()
      .map(checkException(e-> BeanMapper.convert(e,clazz,propPaths)))
      .collect(Collectors.toList());
  }

  @Override
  public <T, R> List<R> findAllById(Iterable<Serializable> ids, Class<T> clazz,
    Class<R> resultType) {
    return findAllById(ids,clazz).stream()
      .map(e->entityMapper(e,clazz,resultType))
      .collect(Collectors.toList());
  }

  @Override
  public <T> T getReference(Serializable id, Class<T> clazz) {
    return em.getReference(clazz, id);
  }

  @Override
  public <T> List<T> namedTypedQuery(Class<T> clazz, String name, Object... params) {
    String entityName = em.getMetamodel().entity(clazz).getName();
    TypedQuery<T> query = em.createNamedQuery(entityName+"."+name,clazz);
    for(int i=0; i<params.length; i++) query.setParameter(i,params[i]);
    return query.getResultList();
  }

  @Override
  public List<?> namedQuery(Class<?> clazz,String name, Object... params) {
    String entityName = em.getMetamodel().entity(clazz).getName();
    Query query = em.createNamedQuery(entityName+"."+name);
    for(int i=0; i<params.length; i++) query.setParameter(i,params[i]);
    return query.getResultList();
  }

  @Override
  public Object namedQuerySingle(Class<?> clazz,String name, Object... params) {
    String entityName = em.getMetamodel().entity(clazz).getName();
    Query query = em.createNamedQuery(entityName+"."+name);
    for(int i=0; i<params.length; i++) query.setParameter(i,params[i]);
    return query.getSingleResult();
  }

  @Override
  public Class<?> getEntityClassByName(String entityTypeName) {
    String[] splitted = entityTypeName.split("\\.");
    String entityTypeSimpleName = splitted[splitted.length-1];
    return em.getMetamodel().getEntities().stream()
      .filter(e-> matchCaseInsensitive(e.getName(),entityTypeSimpleName))
      .findFirst()
      .map(Type::getJavaType)
      .orElse(null);
  }

  @Override
  public String getIdFieldName(Class<?> entityClass){
    return em.getMetamodel().getEntities().stream()
      .filter(e->e.getJavaType().equals(entityClass))
      .findFirst()
      .map(e->e.getId(e.getIdType().getJavaType()).getName())
      .orElse(null);
  }

  private static boolean matchCaseInsensitive(String str1, String str2){
    return str1.equalsIgnoreCase(str2);
  }


  private <T,R> R entityMapper(T entity,Class<T> sourceClass,Class<R> resultType){
    return BeanMapper.convert(entity,sourceClass,resultType);
  }

  private <T> TypedQuery<T> createQuery(AbstarctJpaQuery<?> params,Class<T> typeAnswer,
    String select, String entityName){

    String jpql = params.getJpql();

    if (jpql==null){
      jpql = String.format("Select %s %s from %s %s ",
        params.isDistinct()?"distinct":"",
        select,
        entityName,
        ROOT_CLASS_ALIAS
      );

      if (params.getWhere()!=null) jpql+=" where "+params.getWhere();
      if (params.getGroupBy()!=null) jpql+=" group by "+params.getGroupBy();
      if (params.getHaving()!=null) jpql+=" having "+params.getHaving();
      if (params.getOrderBy()!=null) jpql+=" order by "+params.getOrderBy();
    }


    TypedQuery<T> query = em.createQuery(jpql, typeAnswer);

    if (params.getLockModeType()!=null) query.setLockMode(params.getLockModeType());

    if (params.getSkip()!=null) query.setFirstResult(params.getSkip());
    if (params.getSize()!=null) query.setMaxResults(params.getSize());

    if (params.getQueryParams()!=null){
      for(int i=0; i<params.getQueryParams().length; i++){
        query.setParameter(i,params.getQueryParams()[i]);
      }
    }else{
      params.getQueryNamedParams().forEach(query::setParameter);
    }


    params.getHints().forEach(query::setHint);

    return query;
  }

  private <T> List<Tuple> queryScalarTuple(ScalarJpaQuery<T> params){
    EntityType<T> entityType = em.getMetamodel().entity(params.getClazz());

    TypedQuery<Tuple> query = createQuery(params,
      Tuple.class,
      String.join(",",params.getSelects()),
      entityType.getName()
      );

    boolean isAggregate =  Stream.of(params.getSelects())
      .allMatch(s->
        Stream.of(AGGREGATE_FUNCTIONS)
          .anyMatch(f->s.toLowerCase().contains(f))
      );

    return isAggregate
      ? Collections.singletonList(query.getSingleResult())
      :query.getResultList();
  }

  private <T,R> Function<T,R> checkException(CheckedFunction<T,R> checkedFunction) {
    return t -> {
      try {
        return checkedFunction.apply(t);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  @FunctionalInterface
  private interface CheckedFunction<T,R> {
    R apply(T t) throws Exception;
  }

  private void applyFetchGraph(Class<?> clazz,String entityName,TypedQuery<?> query, String[] fetchPath){
    String entityGraphName = entityName+".fetch_"+String.join("_",fetchPath);
    EntityGraph<?> entityGraph = Jpa21Utils
        .tryGetFetchGraph(em,entityGraphName,fetchPath,clazz);
    query.setHint("javax.persistence.fetchgraph", entityGraph);
  }

}
