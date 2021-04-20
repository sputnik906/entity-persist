package com.github.sputnik906.persist.spring.controller.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.persistence.Entity;
import javax.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldNameConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.github.sputnik906.persist.api.repository.EntityRepository;
import com.github.sputnik906.persist.api.repository.query.EntityJpaQuery;

@RequiredArgsConstructor
@RestController
public class EntityController<T,ID extends Serializable> {

  public final static String PROPS = "props";
  public final static String PROP_NAME = "propName";

  protected final Class<T> domainClass;
  protected final Class<ID> idClass;

  protected final EntityRepository repository;

  public static <T,ID extends Serializable> EntityController<T,ID> from(
    Class<T> domainClass,Class<ID> idClass,EntityRepository repository){
    return new EntityController<>(domainClass,idClass,repository);
  }

  @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
  public T create(
    @RequestBody Map<String,Object> dto) {
    return repository.persist(createFrom(repository,domainClass,dto));
  }

  @RequestMapping(value = "/batch", method = RequestMethod.POST, consumes = "application/json")
  public List<T> createBatch(
    @RequestBody List<Map<String,Object>> dtos) {
    return repository.persistAll(dtos.stream()
      .map(dto->createFrom(repository,domainClass,dto))
      .collect(Collectors.toList()));
  }

  //Openapi 3 не позволяет объявлять зависимости с параметрами или взаимоисключающими параметрами
  //https://github.com/OAI/OpenAPI-Specification/issues/256
  //https://starkovden.github.io/step4-paths-object.html#%D0%BF%D1%80%D0%B8%D0%BC%D0%B5%D1%87%D0%B0%D0%BD%D0%B8%D0%B5-%D0%BE-%D0%B7%D0%B0%D0%B2%D0%B8%D1%81%D0%B8%D0%BC%D0%BE%D1%81%D1%82%D1%8F%D1%85-%D0%BF%D0%B0%D1%80%D0%B0%D0%BC%D0%B5%D1%82%D1%80%D0%BE%D0%B2
  //взятие всех сущностей через обычный список, с пагенацией, по id, namedquery кодируем
  //через разные path

  @RequestMapping(method = RequestMethod.GET)
  public List<?> findAll(
    @RequestParam(value = "search", required = false) String search,
    @RequestParam(value = "order", required = false) String order,
    @RequestParam(value = PROPS,required = false) String[] props
  ) {
    EntityJpaQuery<T> q = new EntityJpaQuery<>(domainClass);
    q.setWhere(search);
    q.setOrderBy(order);

    List<?> content = props!=null
      ?repository.query(q,props)
      :repository.query(q);

    return content;
  }

  @RequestMapping(value = "/paged", method = RequestMethod.GET)
  public Page<?> findAllPaged(
    @RequestParam(value = "page", required = false, defaultValue = "0") int page,
    @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
    @RequestParam(value = "search", required = false) String search,
    @RequestParam(value = "order", required = false) String order,
    @RequestParam(value = PROPS,required = false) String[] props
  ) {
    EntityJpaQuery<T> q = new EntityJpaQuery<>(domainClass);
    q.setSize(size);
    q.setSkip(page*size);
    q.setWhere(search);
    q.setOrderBy(order);

    List<?> content = props!=null
      ?repository.query(q,props)
      :repository.query(q);

    return new Page<>(content,page,size);
  }



  @RequestMapping(value = "/namedquery",method = RequestMethod.GET)
  public List<?> findAllNamedQuery(
    @RequestParam(value = "q") String namedQuery,
    @RequestParam(value = "params", required = false) String[] params
  ){
   return repository.namedQuery(
      domainClass,
      namedQuery,
      params!=null
        ?params
        :new Object[0]
    );
  }

  @RequestMapping(value = "/namedquerysingle",method = RequestMethod.GET)
  public Object findAllNamedQuerySingle(
    @RequestParam(value = "q") String namedQuery,
    @RequestParam(value = "params", required = false) String[] params
  ){
    return repository.namedQuerySingle(
      domainClass,
      namedQuery,
      params!=null
        ?params
        :new Object[0]
    );
  }

  @RequestMapping(value = "/ids",method = RequestMethod.GET,params = "ids")
  public List<?> findAllById(
    @RequestParam(value = "ids") ID[] ids,
    @RequestParam(value = PROPS,required = false) String[] props
  ) {
    return props!=null
      ?repository.findAllById(from(ids),domainClass,props)
      :repository.findAllById(from(ids),domainClass);
  }

  @RequestMapping(value = "/count",method = RequestMethod.GET)
  public Object count(
    @RequestParam(value = "search", required = false) String search
  ) {
    return repository.aggregateFun(
      "count",
      null,
      search,
      domainClass
    );
  }

  @RequestMapping(value = "/{fun:(?:avg|sum|min|max)}",method = RequestMethod.GET,params = "field")
  public Object aggregateFun(
    @PathVariable(value = "fun") String fun,
    @RequestParam(value = "field") String field,
    @RequestParam(value = "search", required = false) String search
  ) {
    return repository.aggregateFun(
      fun,
      field,
      search,
      domainClass
    );
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public Optional<?> findById(
    @PathVariable(value = "id") ID id,
    @RequestParam(value = PROPS,required = false) String[] props
  ){
    return props!=null
      ?repository.findById(id,domainClass,props)
      :repository.findById(id,domainClass);
  }

  @RequestMapping(value = "/{id}/{"+PROP_NAME+"}", method = RequestMethod.GET)
  public Optional<?> readProps(
    @PathVariable(value = "id") ID id,
    @PathVariable(value = PROP_NAME) String propName) {
    return repository.findById(id,domainClass,new String[]{propName});
  }

  @RequestMapping(method = RequestMethod.PATCH)
  public Optional<?> patch(
    @RequestBody Map<String, Object> patch
  ) {
    int v = (int)patch.get("version");
    return repository.patch(
      from((Serializable) patch.get("id")).get(0),
      (long)v,
      domainClass,
      patch
    );
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
  public Optional<?> patch(
    @PathVariable(value = "id") ID id,
    @RequestBody Map<String, Object> patch
  ) {
    return repository.patch(
      id,
      (Long)patch.get("version"),
      domainClass,
      patch
    );
  }

  @RequestMapping(value = "/{id}/{"+PROP_NAME+"}", method = RequestMethod.POST, consumes= MediaType.APPLICATION_JSON_VALUE)
  public Optional<?> setPropObjectType(
    @PathVariable(value = "id") ID id,
    @PathVariable(value = PROP_NAME) String propName,
    @RequestBody Map<String, Object> value) {
    Map<String, Object> patch = new HashMap<>();
    patch.put(propName,value);
    return patch(id,patch);
  }

  @RequestMapping(value = "/{id}/{"+PROP_NAME+"}", method = RequestMethod.POST, consumes= MediaType.TEXT_PLAIN_VALUE)
  public Optional<?> setPropSimpleType(
    @PathVariable(value = "id") ID id,
    @PathVariable(value = PROP_NAME) String propName,
    @RequestBody Object value) {
    Map<String, Object> patch = new HashMap<>();
    patch.put(propName,value);
    return patch(id,patch);
  }

  @RequestMapping(value = "/{id}/{"+PROP_NAME+"}/add", method = RequestMethod.POST)
  public Optional<?> addToCollectionProp(
    @PathVariable(value = "id") ID id,
    @PathVariable(value = PROP_NAME) String propName,
    @RequestBody @Valid List<Object> ids
  ) {
    return repository.inTransactionWithResult(r->invokeMethod(id,propName,ids,"add"));
  }
  @RequestMapping(value = "/{id}/{"+PROP_NAME+"}/remove", method = RequestMethod.DELETE)
  public Optional<?> removeFromCollectionProp(
    @PathVariable(value = "id") ID id,
    @PathVariable(value = PROP_NAME) String propName,
    @RequestBody @Valid List<Object> ids
  ) {
    return repository.inTransactionWithResult(r->invokeMethod(id,propName,ids,"remove"));
  }

  @RequestMapping(value = "/{id}/{"+PROP_NAME+"}/clear", method = RequestMethod.DELETE)
  public Optional<?> clearCollection(
    @PathVariable(value = "id") ID id,
    @PathVariable(value = PROP_NAME) String propName
  ) {
    return repository.inTransactionWithResult(r->clearCollectionProp(id,propName));
  }

  @RequestMapping(
    value = "/m/{methodName}",
    method = RequestMethod.POST,
    consumes= MediaType.APPLICATION_JSON_VALUE)
  public Object invokeStaticMethod(
    @PathVariable(value = "methodName") String methodName,
    @RequestBody(required = false) ArrayNode arrayNode
  ) {
    Object[] values = arrayNode!=null
      ? StreamSupport.stream(arrayNode.spliterator(), false).toArray(Object[]::new)
      :new Object[0];
    return repository.inTransactionWithResult(r->invokeMethod(null,methodName,values));
  }

  @RequestMapping(
    value = "/{id}/m/{methodName}",
    method = RequestMethod.POST,
    consumes= MediaType.APPLICATION_JSON_VALUE)
  public Optional<Object> invokeMethodAtInstance(
    @PathVariable(value = "id") ID id,
    @PathVariable(value = "methodName") String methodName,
    @RequestBody(required = false) ArrayNode arrayNode
  ) {
    Object[] values = arrayNode!=null
      ?StreamSupport.stream(arrayNode.spliterator(), false).toArray(Object[]::new)
      :new Object[0];
    return repository.inTransactionWithResult(r->r.findById(id,domainClass)
      .map(instance->invokeMethod(instance,methodName,values))
    );
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public void delete(@PathVariable(value = "id") ID id) {
    repository.deleteById(id,domainClass);
  }

  @RequestMapping(params = "ids", method = RequestMethod.DELETE)
  public void deleteIds(@RequestParam(value = "ids") ID[] ids) {
    repository.deleteAllById(from(ids),domainClass);
  }

  @RequiredArgsConstructor
  @Getter
  @FieldNameConstants
  public static class Page<T>{
    private final List<T> content;
    private final int page;
    private final int size;
  }

  @SneakyThrows
  private static <D> D createFrom(EntityRepository repository,Class<D> destClass,Map<String,Object> dto){
    Constructor<?>[] fittedConstructors = Stream.of(destClass.getDeclaredConstructors())
      .filter(c -> Modifier.isPublic(c.getModifiers()))
      .filter(c -> c.isAnnotationPresent(ConstructorProperties.class))//generate lombook
      .filter(c -> c.getParameterCount() > 0)
      .toArray(Constructor[]::new);

    Constructor<?> theBestMatchConstructor =  bestMatch(fittedConstructors,dto);

    if (theBestMatchConstructor==null) throw new IllegalArgumentException();

    Constructor<D> constructor = destClass.getConstructor(theBestMatchConstructor.getParameterTypes());

    String[] constructorNameProps = constructor.getAnnotation(ConstructorProperties.class).value();

    Object[] params = Stream.of(constructorNameProps)
      .map(dto::get)
      .toArray();

    Class<?>[] parameterTypes = constructor.getParameterTypes();

    ObjectMapper mapper = new ObjectMapper();

    for(int i=0; i<parameterTypes.length; i++){
      if (Collection.class.isAssignableFrom(parameterTypes[i])){
        Collection<?> collection = ((Collection<?>) params[i]);

        Class<?> genericTypeInDst = (Class<?>)((ParameterizedType)constructor.getGenericParameterTypes()[i]).getActualTypeArguments()[0];

        if (genericTypeInDst.isAnnotationPresent(Entity.class)){
          //TODO предусмотреть возможнотсь заменить на reference
          params[i] = repository.findAllById((Collection<Serializable>)collection,genericTypeInDst).stream()
            .collect(getCollectors(parameterTypes[i]));
        }else{
          params[i] = collection.stream().collect(getCollectors(parameterTypes[i]));
        }
      }else{
        params[i] = mapper.convertValue(params[i],parameterTypes[i]);
      }

    }

    D instance = constructor.newInstance(params);

   dto.keySet().stream()
      .filter(p-> !Arrays.asList(constructorNameProps).contains(p) )
      .filter(p-> dto.get(p) != null )
      .filter(p-> Collection.class.isAssignableFrom(dto.get(p).getClass()) )
      .filter(p-> !((Collection<?>)dto.get(p)).isEmpty())
      .forEach(p->{
        Stream.of(destClass.getDeclaredMethods())
          .filter(m -> Modifier.isPublic(m.getModifiers()))
          .filter(m->m.getName().equals(methodName("add",p)))
          .filter(m->m.getParameterCount()==1)
          .filter(m->Collection.class.isAssignableFrom(m.getParameterTypes()[0]))
          .findFirst()
          .ifPresent(m->{
            Class<?> genericType = (Class<?>)((ParameterizedType)m.getGenericParameterTypes()[0]).getActualTypeArguments()[0];
            Collection<?> convertedValue = ((Collection<?>)dto.get(p)).stream()
              .map(e->Map.class.isAssignableFrom(e.getClass())
                ?createFrom(repository,genericType,(Map<String,Object>)e)
                :e
              ).collect(Collectors.toCollection(collectionFactory(m.getParameterTypes()[0])));
            invoke(m,instance,convertedValue);
          });
      });


    return instance;

  }

  private static Constructor<?> bestMatch(Constructor<?>[] constructors, Map<String,Object> dto){
    Constructor<?> theBestMatchConstructor = null;
    for(Constructor<?> constructor:constructors){
      String[] props = constructor.getAnnotation(ConstructorProperties.class).value();
      Set<String> propsSet =  new HashSet<>(Arrays.asList(props));
      if (props.length!=dto.size()) break;
      for(String str:propsSet) if (!dto.containsKey(str)) break;
      theBestMatchConstructor = constructor;
      break;
    }
    if (theBestMatchConstructor==null){
      for(Constructor<?> constructor:constructors){
        String[] props = constructor.getAnnotation(ConstructorProperties.class).value();
        Set<String> propsSet =  new HashSet<>(Arrays.asList(props));
        if (props.length>dto.size()) break;
        for(String str:propsSet) if (!dto.containsKey(str)) break;
        theBestMatchConstructor = constructor;
        break;
      }
    }
    return theBestMatchConstructor;
  }


  @SneakyThrows
  private static <T> Constructor<T> getDeclaredConstructor(Class<T> sourceClass,Constructor<?> c){
    return sourceClass.getDeclaredConstructor(c.getParameterTypes());
  }

  private boolean hasEntityRepoFirstParam(Method m){
    if (m.getParameterCount()==0) return false;
    return EntityRepository.class.equals(m.getParameterTypes()[0]);
  }

  private Object invokeMethod(Object instance,String methodName, Object ... values){
    Method method = Stream.of(domainClass.getDeclaredMethods())
      .filter(m-> instance != null || Modifier.isStatic(m.getModifiers()))
      .filter(m->m.getName().equals(methodName))
      .filter(m->m.getParameterCount()==values.length+(hasEntityRepoFirstParam(m)?1:0))
      .findFirst().orElseThrow(IllegalArgumentException::new);
    method.setAccessible(true);

    ObjectMapper mapper = new ObjectMapper();

    if (hasEntityRepoFirstParam(method)){
      Object[] convertedValues = new Object[values.length+1];
      convertedValues[0] = repository;
      for(int i=0; i< values.length; i++){
        convertedValues[i+1]=mapper.convertValue(values[i],method.getParameterTypes()[i+1]);
      }
      return invoke(method,instance,convertedValues);
    }

    Object[] convertedValues = new Object[values.length];

    for(int i=0; i< convertedValues.length; i++){
      convertedValues[i]=mapper.convertValue(values[i],method.getParameterTypes()[i]);
    }
    return invoke(method,instance,convertedValues);
  }

  private List<Serializable> from(Serializable ... ids){
    ObjectMapper mapper = new ObjectMapper();
    return Stream.of(ids)
      .map(id->mapper.convertValue(id,idClass))
      .collect(Collectors.toList());
  }

  private Optional<Object> invokeMethod(ID id, String propPath, Collection<?> values,String prefix){

    return repository.findById(id,domainClass)
      .map(instance->{
        Method method = getMethod(methodName(prefix,propPath),Collection.class);

        Class<?> elementType = (Class<?>) ((ParameterizedType)method.getGenericParameterTypes()[0]).getActualTypeArguments()[0];

        boolean isEntity = elementType.isAnnotationPresent(Entity.class);

        Collection<?> mappedValues =  values.stream()
          .map(elId->isEntity?repository.getReference((Serializable) elId,elementType):elId)
          .collect(Collectors.toList());

        invoke(method,instance,mappedValues);

        return instance;

      });

  }

  private static String methodName(String prefix,String propPath){
    return prefix+propPath.substring(0, 1).toUpperCase() + propPath.substring(1);
  }

  private static Supplier<Collection<Object>> collectionFactory(Class<?> collectClass){
    if (Set.class.isAssignableFrom(collectClass)) return HashSet::new;
    if (List.class.isAssignableFrom(collectClass)) return ArrayList::new;
    if (Collection.class.equals(collectClass)) return HashSet::new;
    throw new IllegalArgumentException();
  }

  private static  Collector<Object, ?, ?> getCollectors(Class<?> collectClass){
    if (Set.class.isAssignableFrom(collectClass)) return Collectors.toSet();
    if (List.class.isAssignableFrom(collectClass)) return Collectors.toList();
    if (Collection.class.equals(collectClass)) return Collectors.toSet();
    throw new IllegalArgumentException();
  }



  private Optional<Object> clearCollectionProp(ID id, String propPath) {

    return repository.findById(id,domainClass).map(instance->{
      Method clearMethod = getMethod(methodName("clear",propPath));

      invoke(clearMethod,instance);

      return instance;
    });

  }

  @SneakyThrows
  private Method getMethod(String name, Class<?>... parameterTypes){
    Method method = domainClass.getMethod(name,parameterTypes);
    method.setAccessible(true);
    return method;
  }

  @SneakyThrows
  public static  Object invoke(Method method,Object obj, Object... args){
    return method.invoke(obj,args);
  }

}
