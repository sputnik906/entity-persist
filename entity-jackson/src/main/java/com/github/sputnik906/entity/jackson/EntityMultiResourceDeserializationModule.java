package com.github.sputnik906.entity.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import com.gitlab.sputnik906.lang.utils.BeanUtils;

/**
 * Jackson модуль предназначен для загрузки связанных сущностей из различных источников (например файлов).
 * Данные сущности ссылаются друг на друга через id.
 * Принцип работы:
 * Стандартный jackson десериализатор для класса с аннотацией @Entity оборачивается в обертку.
 * Эта обертка пытается сохранить каждое десериализуемое значение сущности в dataset. Если в dataset есть уже
 * инстанст этой сущности с таким id, то в этот инстанс копируется все значения десериализируемой сущности
 * Допустим десериализуется сущность, у нее есть ссылка на другую сущность указанная через id,
 * стандартынй десериализатор в этом случае вызовит обработчик handleMissingInstantiator, в нем по
 * id попытаемся найти в dataset уже загруженный экземпляр этой сущности, либо в противном случае
 * создадим пустой инстанс этой сущности, и вернем ее как значение поля и сохраним ее в dataset,
 * в дальнейшем поля сущности заполниться по алгоритму описанному выше.
 */
public class EntityMultiResourceDeserializationModule extends SimpleModule {
  @Override
  public void setupModule(SetupContext context) {

    //StateFull карта - <класс сущности, <id сущности, инстанс сущности>.
    Map<Class<?>, Map<Object,Object>> dataset = new HashMap<>();

    super.setupModule(context);

    context.addDeserializationProblemHandler(new DeserializationProblemHandler() {

      @Override
      public Object handleMissingInstantiator(DeserializationContext ctxt, Class<?> instClass,
        ValueInstantiator valueInsta,
        JsonParser p, String msg) throws IOException {

        Class<?> idClass = BeanUtils.findFirstFieldWith(Id.class,instClass).getType();

        Object id = ctxt.findNonContextualValueDeserializer(ctxt.constructType(idClass)).deserialize(p,ctxt);

        Map<Object,Object> idToObjectMap = dataset.computeIfAbsent(instClass,k->new HashMap<>());
        Object value = idToObjectMap.get(id);
        if (value==null){
          value = valueInsta.createUsingDefault(ctxt);
          idToObjectMap.put(id,value);
        }
        return value;

      }
    });

    context.addBeanDeserializerModifier(new BeanDeserializerModifier(){
      @Override
      public JsonDeserializer<?> modifyDeserializer(
        DeserializationConfig config, BeanDescription beanDesc,
        JsonDeserializer<?> deserializer) {
        if (deserializer instanceof BeanDeserializer
          && beanDesc.getBeanClass().isAnnotationPresent(Entity.class))
          return super.modifyDeserializer(config, beanDesc,new EntityDeserializationWrapper(deserializer,dataset));
        else
          return deserializer;
      }
    });
  }

  @RequiredArgsConstructor
  private static class EntityDeserializationWrapper extends JsonDeserializer<Object> implements
    ResolvableDeserializer {

    @NonNull
    private final JsonDeserializer<?> delegateDeserializer;
    @NonNull
    private final Map<Class<?>,Map<Object,Object>> dataset;

    @SneakyThrows
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {

      Object deseredValue = delegateDeserializer.deserialize(p,ctxt);
      Map<Object,Object> idToObjectMap = dataset.computeIfAbsent(deseredValue.getClass(),k->new HashMap<>());
      Field fieldId = BeanUtils.findFirstFieldWith(Id.class,deseredValue.getClass());
      Object id = fieldId.get(deseredValue);
      if (id==null) return deseredValue;
      Object storedValue = idToObjectMap.get(id);
      if (storedValue==null){
        storedValue=deseredValue;
        idToObjectMap.put(id,storedValue);
      }else{
        if (storedValue!=deseredValue)
          BeanUtils.copyFieldStates(storedValue,deseredValue);
      }

      return storedValue;
    }

    // for some reason you have to implement ResolvableDeserializer when modifying BeanDeserializer
    // otherwise deserializing throws JsonMappingException??
    @Override public void resolve(DeserializationContext ctxt) throws JsonMappingException
    {
      ((ResolvableDeserializer) delegateDeserializer).resolve(ctxt);
    }
  }
}
