package com.github.sputnik906.entity.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.persistence.Id;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.gitlab.sputnik906.lang.utils.BeanUtils;
import com.gitlab.sputnik906.lang.utils.Maps;

/**
 * Jackson фильтр - при сериализации сущностей, если поле ссылается на сущность представленную в
 * witchEntityClassChangeId, инстанс сущности заменяется на id
 */
@RequiredArgsConstructor
public class ChangeEntityRelationOnIdFilter extends SimpleBeanPropertyFilter {

  public final static String ID="changeEntityRelationOnIdFilter";

  @NonNull
  private final Set<Class<?>> witchEntityClassChangeId;

  @NonNull
  private final boolean printIdAndLabel;

  @Override
  public void serializeAsField(Object pojo, JsonGenerator jgen,
    SerializerProvider provider, PropertyWriter writer)
    throws Exception
  {
    if (include(writer)) {
      if (writer.getType().hasContentType()) { //если поле типа Collection
        Class<?> fieldClass = writer.getType().getContentType().getRawClass();

        if (witchEntityClassChangeId.contains(fieldClass)){

          Field fieldId = BeanUtils.findFirstFieldWith(Id.class,fieldClass);

          Object propertyValue = BeanUtils.getNestedProperty(pojo,writer.getName());

          List<Object> values = new ArrayList<>();
          for(Object p:((Collection<?>) propertyValue))
            values.add(getValue(
              fieldId,
              p
            ));

          provider.defaultSerializeField(writer.getName(),values,jgen);

        }else{
          writer.serializeAsField(pojo, jgen, provider);
        }
      }else{ // если простое поле
        if (witchEntityClassChangeId.contains(writer.getType().getRawClass())){

          Field fieldId = BeanUtils.findFirstFieldWith(Id.class,writer.getType().getRawClass());

          Object propertyValue = BeanUtils.getNestedProperty(pojo,writer.getName());

          provider.defaultSerializeField(
            writer.getName(),
            getValue(fieldId,propertyValue),
            jgen
          );

        }else{
          writer.serializeAsField(pojo, jgen, provider);
        }
      }
    }

  }

  private Object getValue(Field fieldId,Object propertyValue){
    //Берем значение через геттер, нужно при работе с proxy от Hibernate
    Object id = BeanUtils.getNestedProperty(propertyValue, fieldId.getName());
    Object value;
    if (printIdAndLabel){
      Object label = null;
      try{
        label = BeanUtils.getNestedProperty(propertyValue, "label");
      }catch (Exception e){
        //ignory
      }
       value = Maps.of(fieldId.getName(),id,"label",label);
    }else{
      value = id;
    }
    return value;
  }


}
