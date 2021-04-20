package com.github.sputnik906.entity.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.experimental.UtilityClass;
import com.gitlab.sputnik906.lang.utils.FileUtils;
import com.gitlab.sputnik906.persist.api.DataCollectionParam;


@UtilityClass
public class WriteData {
  public static void toYml(
    Map<Class<?>, DataCollectionParam> dataset,
    File rootFile) throws IOException {

    File baseDir = rootFile.getParentFile();

    FileUtils.createOrClearDir(baseDir);

    rootFile.createNewFile();

    YAMLFactory yamlFactory = new YAMLFactory();
    ObjectMapper objectMapper = new ObjectMapper(yamlFactory);

    //objectMapper.addMixIn(Object.class,AllObjectMixin.class);//не работает mixIn на Object.class
    //Убираем у всех инстансов от классов с аннотацией Entity печать версии
    //реализовать это требования через фильтр и мексины не удалось
    objectMapper.registerModule(new SimpleModule(){
      @Override
      public void setupModule(SetupContext context) {
        super.setupModule(context);

        context.addBeanSerializerModifier(new BeanSerializerModifier() {
          @Override
          public List<BeanPropertyWriter> changeProperties(
            SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {

            if (beanDesc.getBeanClass().isAnnotationPresent(Entity.class)){
              return beanProperties.stream()
                .filter(beanPropertyWriter -> !beanPropertyWriter.getName().equals("version"))
                //убираем технические записи - для поддержки консистентности
                .filter(beanPropertyWriter -> !(
                  beanPropertyWriter.getAnnotation(OneToMany.class)!=null
                  &&beanPropertyWriter.getAnnotation(OneToMany.class).mappedBy().length()>0
                  &&dataset.containsKey(beanPropertyWriter.getType().getContentType().getRawClass()))
                ).filter(beanPropertyWriter ->!(
                  beanPropertyWriter.getAnnotation(ManyToOne.class)!=null
                  &&!dataset.containsKey(beanDesc.getBeanClass()))
                )
                .collect(Collectors.toList());
            }
            return beanProperties;
          }
          @Override
          public BeanSerializerBuilder updateBuilder(SerializationConfig config,
            BeanDescription beanDesc, BeanSerializerBuilder builder) {

            if (beanDesc.getBeanClass().isAnnotationPresent(Embeddable.class)){
              builder.setFilterId(ChangeEntityRelationOnIdFilter.ID);
            }

            if (beanDesc.getBeanClass().isAnnotationPresent(Entity.class)){
              builder.setFilterId(ChangeEntityRelationOnIdFilter.ID);
            }


            return builder;
          }
        });
      }
    });

    //objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    SimpleFilterProvider filters = new SimpleFilterProvider();

    filters.addFilter(
      ChangeEntityRelationOnIdFilter.ID,
      new ChangeEntityRelationOnIdFilter(dataset.keySet(),false)
    );

    Map<String,List<String>> reference = new LinkedHashMap<>();
    dataset.keySet().forEach(entityClass->reference.put(
      entityClass.getSimpleName(),
      Collections.singletonList(
        Optional.ofNullable(dataset.get(entityClass).getName())
          .orElse(entityClass.getSimpleName())+".yml")
    ));

    objectMapper
      .writerWithDefaultPrettyPrinter()
      .writeValue(rootFile,reference);

    for(Map.Entry<Class<?>, DataCollectionParam> entry: dataset.entrySet()){
      objectMapper
        .setFilterProvider(filters)
        .writerWithDefaultPrettyPrinter()
        .writeValue(new File(baseDir, reference.get(entry.getKey().getSimpleName()).get(0)),entry.getValue().getEntities());
    }
  }

}
