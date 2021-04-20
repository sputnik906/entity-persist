package com.github.sputnik906.entity.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import com.gitlab.sputnik906.lang.utils.BeanUtils;

@UtilityClass
public class ReadData {
  public static Map<Class<?>, List<?>> fromYml(File rootFile, Set<Class<?>> entityClasses)
    throws IOException, NoSuchFieldException, IllegalAccessException {
    YAMLFactory yamlFactory = new YAMLFactory();
    ObjectMapper objectMapperReader = new ObjectMapper(yamlFactory);
    objectMapperReader.registerModule(new EntityMultiResourceDeserializationModule());

    Map<Class<?>, List<?>> dataset = new LinkedHashMap<>();

    Map<String,List<String>> reference = objectMapperReader.readValue(rootFile,new TypeReference<LinkedHashMap<String,List<String>>>(){});

    File baseDir = rootFile.getParentFile();

    reference.forEach((entityName,fileNames)->{

      Class<?> entityClass = entityClasses.stream()
        .filter(e->e.getSimpleName().equals(entityName))
        .findFirst()
        .orElse(null);

      if (entityClass==null){
        System.err.println("Unknown entity name:"+entityName);
        return;
      }

      JavaType javaType = objectMapperReader.getTypeFactory().constructParametricType(List.class, entityClass);

      List<?> entities = fileNames.stream()
        .flatMap(fileName->((List<?>)readValue(objectMapperReader,new File(baseDir,fileName),javaType)).stream())
        .collect(Collectors.toList());

      dataset.put(entityClass,entities);
    });

    for(Class<?> entityClass:dataset.keySet())
      trySetMissedField(entityClass,dataset);

    return dataset;

  }

  /**
   * Некоторые поля сущностей, предназначенные например для каскада, могут быть не заполнены
   * в файлах, поэтому пытаемся их восстановить
   * @param dataset
   */
  private static void trySetMissedField(Class<?> dstClass,Map<Class<?>, List<?>> dataset)
    throws NoSuchFieldException, IllegalAccessException {
    for(Field f:BeanUtils.listAllNonStaticFields(dstClass)){
      if (f.isAnnotationPresent(OneToMany.class)
        &&f.getAnnotation(OneToMany.class).mappedBy().length()>0){

        Class<?> genericTypeClass = BeanUtils.getCollectionGenericClassOrNull(f);
        Field referenceField = genericTypeClass.getDeclaredField(f.getAnnotation(OneToMany.class).mappedBy());
        f.setAccessible(true);
        referenceField.setAccessible(true);
        for(Object entity:dataset.get(dstClass)){
          trySetCollectionField(entity,f,referenceField,dataset.get(genericTypeClass));
        }
      }
      if (f.isAnnotationPresent(ManyToOne.class)){
        Field refCollectionField =  BeanUtils.listAllNonStaticFields(f.getType()).stream()
            .filter(field->Collection.class.isAssignableFrom(field.getType())
              &&BeanUtils.getCollectionGenericClassOrNull(field).equals(dstClass))
            .findFirst().orElse(null);
        if (refCollectionField==null) continue;
        f.setAccessible(true);
        refCollectionField.setAccessible(true);
        for(Object entity:dataset.get(dstClass)){
          trySetSingleField(entity,f,refCollectionField,dataset.get(f.getType()));
        }
      }
      //Теперь проверка на заполнение дочерних сущностей и которые не находятся в dataset
      if (f.isAnnotationPresent(OneToMany.class)&&
        !dataset.containsKey(BeanUtils.getCollectionGenericClassOrNull(f))){
        for(Object entity:dataset.get(dstClass)){
          Map<Class<?>, List<?>> constraintDataset = new HashMap<>();
          constraintDataset.put(dstClass, Collections.singletonList(entity));
          constraintDataset.put(
            BeanUtils.getCollectionGenericClassOrNull(f),
            new ArrayList<>((Collection<?>)f.get(entity))
          );
          trySetMissedField(
            BeanUtils.getCollectionGenericClassOrNull(f),
            constraintDataset
          );
        }

      }




    }


  }

  private static void trySetSingleField(Object entity,Field fieldDst,Field refCollectionField,List<?> entities)
    throws IllegalAccessException {
    if (fieldDst.get(entity)!=null) return;

    Object fieldValue = entities.stream()
      .filter(e->isContainInCollection(e,refCollectionField,entity))
      .findFirst()
      .orElse(null);

    fieldDst.set(entity,fieldValue);
  }

  @SneakyThrows // для использования в лямде
  private static boolean isContainInCollection(Object testObject,Field refCollectionField,Object refObject){
    return ((Collection<?>)refCollectionField.get(testObject)).contains(refObject);
  }



  private static void trySetCollectionField(Object entity,Field collFieldDst,Field refField,List<?> entities)
    throws IllegalAccessException{

    if (collFieldDst.get(entity)!=null) return;

    Collection<?> fieldValue = entities.stream()
      .filter(e->isReference(e,refField,entity))
      .collect(Collectors.toCollection(()->createCollectionInstanceForType(collFieldDst.getType())));

    collFieldDst.set(entity,fieldValue);

  }

  @SneakyThrows // для использования в лямде
  private static boolean isReference(Object testObject,Field field,Object refObject){
    return refObject.equals(field.get(testObject));
  }

  private static  Collection<Object> createCollectionInstanceForType(Class<?> interfaceClass){
    if (interfaceClass.equals(List.class)) return new ArrayList<>();
    if (interfaceClass.equals(Set.class)) return new HashSet<>();
    throw new IllegalStateException();
  }

  @SneakyThrows // для использования в лямдах
  private static <T> T readValue(ObjectMapper objectMapperReader,File src, JavaType valueType){
    return objectMapperReader.readValue(src,valueType);
  }
}
