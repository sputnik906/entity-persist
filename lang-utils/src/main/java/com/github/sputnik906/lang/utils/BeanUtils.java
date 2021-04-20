package com.github.sputnik906.lang.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BeanUtils {
  @SneakyThrows
  public static Object getNestedProperty(Object bean, String propPath){
    String[] props = propPath.split("\\.");

    Object value = bean;
    for(int i=0; i<props.length; i++){
      BeanInfo beanInfo = Introspector.getBeanInfo(value.getClass());
      int finalI = i;
      PropertyDescriptor propertyDescriptor = Stream.of(beanInfo.getPropertyDescriptors())
          .filter(p->p.getName().equals(props[finalI]))
          .findFirst()
          .orElseThrow(IllegalArgumentException::new);

      value = propertyDescriptor.getReadMethod().invoke(value);

    }

    return value;

  }

  @SneakyThrows
  public static void setProperty(Object bean, String propPath, Object value) {
    String[] props = propPath.split("\\.");

    Object currentBean = bean;
    for(int i=0; i<props.length; i++){

      BeanInfo beanInfo = Introspector.getBeanInfo(currentBean.getClass());
      int finalI = i;
      PropertyDescriptor propertyDescriptor = Stream.of(beanInfo.getPropertyDescriptors())
          .filter(p->p.getName().equals(props[finalI]))
          .filter(p->finalI==props.length-1
              ?p.getWriteMethod()!=null
              :p.getReadMethod()!=null)
          .findFirst()
          .orElseThrow(IllegalArgumentException::new);

      if (i==props.length-1){
        propertyDescriptor.getWriteMethod().invoke(currentBean,value);
      }else{
        currentBean = propertyDescriptor.getReadMethod().invoke(currentBean);
      }
    }
  }

  public static void setProperties(Object bean, Map<String,Object> patch) {
    Map<String, Object> flatMap = convertToFlatMap(patch);
    for(String propPath:flatMap.keySet()){
      setProperty(bean,propPath,flatMap.get(propPath));
    }
  }

  public static  Map<String, Object> convertToFlatMap(Map<String, Object> map){
    Map<String,Object> result = new HashMap<>();
    for(String key:map.keySet()){
      Object value = map.get(key);
      if (Map.class.isAssignableFrom(value.getClass())){
        convertToFlatMap((Map<String,Object>)value).forEach((k,v)->result.put(key+"."+k,v));
      }else{
        result.put(key,value);
      }
    }
    return result;
  }

  public static List<Field> listAllNonStaticFields(Class<?> clazz) {
    List<Field> fieldList = new ArrayList<>();
    Class<?> tmpClass = clazz;
    while (tmpClass != null) {
      fieldList.addAll(Arrays.stream(tmpClass.getDeclaredFields())
        .filter(f-> !Modifier.isStatic(f.getModifiers()))
        .collect(Collectors.toList()));
      tmpClass = tmpClass.getSuperclass();
    }
    return fieldList;
  }

  public static Field findFirstFieldWith(Class<? extends Annotation> annotation,Class<?> clazz){
    Field field = listAllNonStaticFields(clazz).stream()
      .filter(f->f.getAnnotation(annotation)!=null)
      .findFirst()
      .orElseThrow(IllegalArgumentException::new);
    field.setAccessible(true);
    return field;
  }

  public static void copyFieldStates(Object to,Object from){
    if (!to.getClass().equals(from.getClass())) throw new IllegalArgumentException(" class should be equal");

    for(Field f:listAllNonStaticFields(to.getClass())){
      f.setAccessible(true);
      try {
        f.set(to,f.get(from));
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }

  }

  public static Class<?> getCollectionGenericClassOrNull(Field f){
    return (Class<?>)((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];
  }



}
