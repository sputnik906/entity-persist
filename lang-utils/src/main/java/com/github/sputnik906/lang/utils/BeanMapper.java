package com.github.sputnik906.lang.utils;

import java.beans.ConstructorProperties;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BeanMapper {

  private static Map<String, Function<Object,Object>> convertersMap = new ConcurrentHashMap<>();

  static {
    convertersMap.put("Deg",v->Math.toDegrees((Double) v));
  }

  private static Map<Class<?>,Constructor<?>> cashedConstructorMap = new ConcurrentHashMap<>();

  private static Map<Class<?>,Map<String, PathReadMethod>> cashedReadMethodsMap = new ConcurrentHashMap<>();

  private static Map<Constructor<?>,Map<String,String[]>> cashedDestToSourceFlatMap = new ConcurrentHashMap<>();

  private static Map<SourceClassAndProps,Map<String,String[]>> cashedSourceClassAndPropsMap = new ConcurrentHashMap<>();

  public static <S> Map<String,Object> convert(S sourceObject, Class<S> sourceClass, String[] destPathProps){

    Map<String, PathReadMethod> readMethodsMap =
      cashedReadMethodsMap.computeIfAbsent(sourceClass, BeanMapper::getNestedOnlyReadMethods);

    Map<String,String[]> destToSourceFlatMap =
      cashedSourceClassAndPropsMap.computeIfAbsent(
        new SourceClassAndProps(sourceClass,destPathProps),
        e-> match(
          getFlatPropsWithType(readMethodsMap),
          Stream.of(destPathProps)
            .flatMap(pathProp->{
              Set<String> result = new HashSet<>();
              String[] splitted = pathProp.split("\\.");
              for(int i=0; i<splitted.length; i++){
                String r=splitted[0];
                for(int j=1; j<=i; j++) r+="."+splitted[j];
                result.add(r);
              }
              return result.stream();
            })
            .distinct()
            .collect(Collectors.toMap(k->k,k->new DestPropTypeInform(null,null)))
        )
      );

    return convert(
      sourceObject,
      destPathProps,
      "",
      new String[]{},
      destToSourceFlatMap,
      readMethodsMap
    );
  }

  private static <S> Map<String,Object> convert(S sourceObject,
    String[] destPathProps,
    String destPropPrefix,
    String[] sourceStartPropsPath,
    Map<String, String[]> destToSourceFlatMap,
    Map<String, PathReadMethod> readMethodsMap){

    Map<String,Object> result = new HashMap<>();

    for(String destPropPath:destPathProps){

      if (!destPropPath.startsWith(destPropPrefix)) continue;

      String[] destPropPaths = destPropPath.split("\\.");

      Map<String,Object> map = result;
      String currentDestPropPath = destPropPrefix;
      for(int i=0; i<destPropPaths.length; i++){
        currentDestPropPath=currentDestPropPath.isEmpty()
          ?destPropPaths[i]
          :currentDestPropPath+"."+destPropPaths[i];

        Object value = getValueWithSkipStartPropPath(
          sourceObject,
          destToSourceFlatMap.get(currentDestPropPath),
          readMethodsMap,
          sourceStartPropsPath
        );

        if (value==null){
          map.put(destPropPaths[i],null);
          break;
        }

        if (i==destPropPaths.length-1){
          if (Collection.class.isAssignableFrom(value.getClass())){
            map.put(destPropPaths[i],((Collection<?>)value).stream()
              .map(e-> convertValue(destPropPath,e))
              .collect(getCollectors(value.getClass())));
          }else{
            if (Map.class.isAssignableFrom(value.getClass()))
              value = new HashMap<>((Map<?,?>)value);//for lazy load

            map.put(destPropPaths[i],convertValue(destPropPath,value));
          }
        }else{
          //TODO скорее всего вызывается несколько раз для подпути destPropPaths[i]
          if (Collection.class.isAssignableFrom(value.getClass())){
            String finalCurrentDestPropPath = currentDestPropPath;
            value = ((Collection<?>)value).stream()
              .map(e->convert(
                e,
                destPathProps,
                finalCurrentDestPropPath,
                concatenate(sourceStartPropsPath, destToSourceFlatMap.get(finalCurrentDestPropPath)),
                destToSourceFlatMap,
                readMethodsMap
              ))
              .collect(getCollectors(value.getClass()));
            map.put(destPropPaths[i],value);
            break;
          }else if (Map.class.isAssignableFrom(value.getClass())){
            //Пока реализован только первый уровень вложенности
             Map<String,Object> innerMap =  (Map<String,Object>)map.computeIfAbsent(destPropPaths[i],k->new HashMap<>());
             innerMap.put(
               withoutConverters(destPropPaths[i+1]),
               convertValue(destPropPaths[i+1],((Map<?,?>)value).get(withoutConverters(destPropPaths[i+1])))
             );
             break;
          }else{
            map = (Map<String,Object>)map.computeIfAbsent(destPropPaths[i],k->new HashMap<>());
          }

        }

      }

    }
    return result;
  }

  public static <S,D> D convert(S sourceObject, Class<D> destClass){
    return convert(sourceObject,sourceObject.getClass(),destClass);
  }



  public static <S,D> D convert(S sourceObject, Class<?> sourceClass, Class<D> destClass){

    @SuppressWarnings("unchecked")
    Constructor<D> constructor = (Constructor<D>)
      cashedConstructorMap.computeIfAbsent(destClass, BeanMapper::findConstructor);

    Map<String, PathReadMethod> readMethodsMap =
      cashedReadMethodsMap.computeIfAbsent(sourceClass, BeanMapper::getNestedOnlyReadMethods);

    Map<String,String[]> destToSourceFlatMap =
      cashedDestToSourceFlatMap.computeIfAbsent(constructor,c->match(
        getFlatPropsWithType(readMethodsMap),
        getNestedConstructorProps(c)
      ));

    return convert(
      sourceObject,
      "",
      new String[]{},
      constructor,
      destToSourceFlatMap,
      readMethodsMap
    );

  }

  @SneakyThrows
  private static <S,D>  D convert(S sourceObject,String destPropPrefix,
    String[] sourceStartPropsPath,
    Constructor<D> constructor,
    Map<String, String[]> destToSourceFlatMap,
    Map<String, PathReadMethod> readMethodsMap){

    String[] props = constructor.getAnnotation(ConstructorProperties.class).value();
    Class<?>[] paramTypes = constructor.getParameterTypes();
    Object[] params = new Object[paramTypes.length];

    for (int i = 0; i < paramTypes.length; i++) {
      String destPropPath = destPropPrefix.isEmpty()?props[i]:destPropPrefix+"."+props[i];

      Class<?> classType = paramTypes[i];
      if (Collection.class.isAssignableFrom(classType)){
        classType = (Class<?>) ((ParameterizedType)constructor.getGenericParameterTypes()[i]).getActualTypeArguments()[0];
      }

      Constructor<?> nestedConstructor = findConstructor(classType);

      if (Collection.class.isAssignableFrom(paramTypes[i])){
        Collection<?> collection = (Collection<?>)getValueWithSkipStartPropPath(
          sourceObject,
          destToSourceFlatMap.get(destPropPath),
          readMethodsMap,
          sourceStartPropsPath
        );
        if (nestedConstructor==null){
          params[i] = collection.stream()
            .map(e-> convertValue(destPropPath,e))
            .collect(getCollectors(paramTypes[i]));
        }else{
          params[i] = collection.stream()
            .map(e->convert(
              e,
              destPropPath,
              concatenate(sourceStartPropsPath, destToSourceFlatMap.get(destPropPath)),
              nestedConstructor,
              destToSourceFlatMap,
              readMethodsMap
            ))
            .collect(getCollectors(paramTypes[i]));
        }
      }else{
        if (nestedConstructor==null){
          params[i] = getValueWithSkipStartPropPath(
            sourceObject,
            destToSourceFlatMap.get(destPropPath),
            readMethodsMap,
            sourceStartPropsPath
          );
          params[i] = convertValue(destPropPath,params[i]);
        }else{
          params[i] = convert(
            sourceObject,
            destPropPath,
            sourceStartPropsPath,
            nestedConstructor,
            destToSourceFlatMap,
            readMethodsMap
          );
        }
      }

    }
    return constructor.newInstance(params);
  }

  private static <T>  Collector<T, ?, ?> getCollectors(Class<?> collectClass){
    if (Set.class.isAssignableFrom(collectClass)) return Collectors.toSet();
    if (List.class.isAssignableFrom(collectClass)) return Collectors.toList();
    throw new IllegalArgumentException();
  }

  private static Object convertValue(String destProp,Object value){
    return convertersMap.keySet().stream()
      .filter(destProp::endsWith)
      .findFirst()
      .map(f->convertersMap.get(f).apply(value))
      .orElse(value);
   }

  private static String withoutConverters(String s){
    return convertersMap.keySet().stream()
      .filter(s::endsWith)
      .findFirst()
      .map(f->s.substring(0, s.length() - f.length()))
      .orElse(s);
  }

  private static Map<String,String[]> match(
    Map<String,PathReadMethod> sourceMap,Map<String,DestPropTypeInform> destMap){

    Map<String,String[]> result = new HashMap<>();
    for(Entry<String,DestPropTypeInform> destEntry:destMap.entrySet()){

      //Точное совпадением ключа и типа. Приоритет у него наивысший
      Entry<String,PathReadMethod> sourceEntry = sourceMap.entrySet().stream()
        .filter(e->
            e.getKey().equals(withoutConverters(destEntry.getKey()))
                && (
                  destEntry.getValue().getTypeProp()==null
                  ||destEntry.getValue().getTypeProp().isAssignableFrom(e.getValue().getTypeProp())
                )
        )
        .findFirst()
        .orElse(null);

      // вхождением подстрок и типов
      if (sourceEntry == null) {
        sourceEntry = sourceMap.entrySet().stream()
          .filter(e->
              containsByElement(e.getKey(), withoutConverters(destEntry.getKey()))
                && (
                destEntry.getValue().getTypeProp()==null
                  ||destEntry.getValue().getTypeProp().isAssignableFrom(e.getValue().getTypeProp())
              )
          )
          .findFirst()
          .orElse(null);
      }

      // обработка Map типа
      if (sourceEntry == null) {
        sourceEntry = sourceMap.entrySet().stream()
          .filter(e->Map.class.isAssignableFrom(e.getValue().getTypeProp()))
          .filter(e->
            containsByElementIfEndOfSourceMapType(e.getKey(),withoutConverters(destEntry.getKey()))
          )
          .findFirst()
          .orElse(null);
      }

      String[] propPath = sourceEntry!=null
        ?sourceEntry.getKey().split("\\.")
        :null;

      result.put(destEntry.getKey(),propPath);
    }

    return result;
  }

  private static boolean containsByElementIfEndOfSourceMapType(String source, String test){
    String[] sourceList = source.split("\\.");
    String[] testList = test.split("\\.");

    //Значит передено только значение для map для этого подойдет любая Map
    //if (testList.length==1) return true; // пока не реализовано

    for(int j=1; j<testList.length; j++){//j количество элементов с конца
      int diffSize = sourceList.length-(testList.length-j);
      boolean isBreaken = false;
      for(int i=0; i<(testList.length-j); i++)
        if (!sourceList[i+diffSize].contains(testList[i])){
          isBreaken= true;
          break;
        }
      if (!isBreaken) return true;
    }
    return false;

  }


  private static boolean containsByElement(String source, String test){
    String[] sourceList = source.split("\\.");
    String[] testList = test.split("\\.");
    if (sourceList.length<testList.length) return false;
    int diffSize = sourceList.length-testList.length;
    for(int i=0; i<testList.length; i++)
      if (!sourceList[i+diffSize].contains(testList[i])) return false;
    return true;
  }

  private static Map<String,DestPropTypeInform> getNestedConstructorProps(Constructor<?> constructor){
    Map<String,DestPropTypeInform> result = new HashMap<>();
    String[] props = constructor.getAnnotation(ConstructorProperties.class).value();
    Class<?>[] paramTypes = constructor.getParameterTypes();

    for(int i=0; i<paramTypes.length; i++){
      Class<?> genericTypeProp = null;
      if (Collection.class.isAssignableFrom(paramTypes[i])){
        genericTypeProp = (Class<?>)((ParameterizedType)constructor.getGenericParameterTypes()[i]).getActualTypeArguments()[0];
      }

      result.put(props[i],new DestPropTypeInform(paramTypes[i],genericTypeProp));

      Class<?> beanClass = genericTypeProp!=null?genericTypeProp:paramTypes[i];

      Constructor<?> nestedConstructor = findConstructor(beanClass);

      if (nestedConstructor!=null){
        int finalI = i;
        getNestedConstructorProps(nestedConstructor).forEach((k,v)->result.put(props[finalI]+"."+k,v));
      }
    }
    return result;
  }

  private static <T> Constructor<T> findConstructor(Class<T> sourceClass){
    return Stream.of(sourceClass.getDeclaredConstructors())
      .filter(c->Modifier.isPublic(c.getModifiers()))
      .filter(c->c.isAnnotationPresent(ConstructorProperties.class))//generate lombook
      .filter(c->c.getParameterCount()>0)
      .findFirst()
      .map(c->getDeclaredConstructor(sourceClass,c))
      .orElse(null);
  }

  @SneakyThrows
  private static <T> Constructor<T> getDeclaredConstructor(Class<T> sourceClass,Constructor<?> c){
    return sourceClass.getDeclaredConstructor(c.getParameterTypes());
  }

  private static Map<String, PathReadMethod> getNestedOnlyReadMethods(Class<?> beanClass){
    return getNestedOnlyReadMethods(beanClass, new ArrayList<>());
  }


  @SneakyThrows
  private static Map<String, PathReadMethod> getNestedOnlyReadMethods(Class<?> beanClass,
    List<Class<?>> alreadyInspected) {

    alreadyInspected.add(beanClass);

    Map<String, PathReadMethod> result = new HashMap<>();

    for(PropertyDescriptor propertyDescriptor :
      Introspector.getBeanInfo(beanClass).getPropertyDescriptors()){

      if(propertyDescriptor.getReadMethod()!=null){
        Class<?> propertyType = propertyDescriptor.getPropertyType();

        if (Collection.class.isAssignableFrom(propertyType)){
          propertyType = (Class<?>)((ParameterizedType)propertyDescriptor.getReadMethod().getGenericReturnType()).getActualTypeArguments()[0];
        }

        if (propertyType.equals(Class.class)) continue;

        result.put(propertyDescriptor.getName(),new PathReadMethod(
          propertyDescriptor.getName(),
          propertyDescriptor.getReadMethod(),
          propertyDescriptor.getPropertyType(),
          alreadyInspected.contains(propertyType)//prevent cycle dependents
            ?new HashMap<>()
            :getNestedOnlyReadMethods(propertyType,alreadyInspected)
        ));


      }
    }
    alreadyInspected.remove(alreadyInspected.size()-1);

    return result;
  }

  private static Object getValueWithSkipStartPropPath(Object bean,String[] propsPath,Map<String,
    PathReadMethod> readMethodsMap,String[] startPropsPath){

    if (propsPath==null) return null;

    if (startPropsPath.length==0) return getValue(bean,propsPath,readMethodsMap); //optimization

    String[] currentPropPath = Arrays.copyOfRange(propsPath,startPropsPath.length,propsPath.length);
    Map<String,PathReadMethod> currentReadMethodsMap = readMethodsMap;
    for (String s : startPropsPath) {
      currentReadMethodsMap = currentReadMethodsMap.get(s).getNestedProps();
    }
    return getValue(bean,currentPropPath,currentReadMethodsMap);
  }

  @SneakyThrows
  private static Object getValue(Object bean,String[] propsPath,Map<String, PathReadMethod> readMethodsMap){

    Object value = bean;
    Map<String, PathReadMethod> currentReadMethodsMap =readMethodsMap;
    for (String s : propsPath) {
      value = currentReadMethodsMap.get(s).getReadMethod().invoke(value);
      currentReadMethodsMap = currentReadMethodsMap.get(s).getNestedProps();
      if (value == null)
        break;
    }
    return value;
  }

  private static Map<String,PathReadMethod> getFlatPropsWithType(Map<String, PathReadMethod> readMethodsMap){
    Map<String,PathReadMethod> result = new HashMap<>();
    for(String key:readMethodsMap.keySet()){
      result.put(key,readMethodsMap.get(key));
      getFlatPropsWithType(readMethodsMap.get(key).getNestedProps()).forEach((k,v)->result.put(key+"."+k,v));
    }
    return result;
  }

  private static <T> T[] concatenate(T[] a, T[] b) {
    int aLen = a.length;
    int bLen = b.length;

    @SuppressWarnings("unchecked")
    T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
    System.arraycopy(a, 0, c, 0, aLen);
    System.arraycopy(b, 0, c, aLen, bLen);

    return c;
  }

  @Value
  private static class PathReadMethod{
    String propName;
    Method readMethod;
    Class<?> typeProp;
    Map<String,PathReadMethod> nestedProps;
  }

  @Value
  private static class DestPropTypeInform{
    Class<?> typeProp;
    Class<?> genericTypeProp;
  }

  @Value
  @EqualsAndHashCode
  private static class SourceClassAndProps{
    Class<?> sourceClass;
    String[] destPathProps;
  }

}
