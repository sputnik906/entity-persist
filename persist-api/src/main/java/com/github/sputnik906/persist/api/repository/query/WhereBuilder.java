package com.github.sputnik906.persist.api.repository.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lombok.Getter;

public class WhereBuilder {

  private final StringBuilder where = new StringBuilder();
  @Getter
  private final List<Object> params = new ArrayList<>();

  public WhereBuilder expression(String expression){
    where.append(expression);
    return this;
  }

  public WhereBuilder expression(String expression,Object param){
    where.append(expression);
    where.append(" ?").append(this.params.size());
    this.params.add(param);
    return this;
  }

  public WhereBuilder allMemberOf(Set<Serializable> ids, String collectPropName){
    Iterator<Serializable> iterator = ids.iterator();
    StringBuilder result = new StringBuilder();
    while(iterator.hasNext()){
      result.append(iterator.next()).append(" member of ").append(collectPropName);
      if (iterator.hasNext()) result.append(" and ");
    }
    where.append(result.toString());
    return this;
  }

  public WhereBuilder allMemberOf(String collectPropName, Collection<?> collection){
    return allMemberOf(collectPropName,collection!=null?collection.toArray():new Object[0]);
  }

  public WhereBuilder allMemberOf(String collectPropName,Object ...params){
    for(int i=0; i<params.length; i++){
      where.append(" ?").append(this.params.size()).append(" member of ").append(collectPropName);
      this.params.add(params[i]);
      if (i<params.length-1) where.append(" and ");
    }
    return this;
  }

  public WhereBuilder overlap(String endTimePropName,long startTime,String startTimePropName,long endTime){
    where.append(endTimePropName).append(" > ").append(startTime).append(" and ")
      .append(startTimePropName).append(" < ").append(endTime);
    return this;
  }

  public WhereBuilder and(){
    if(where.toString().trim().length()>0)  where.append(" and ");
    return this;
  }

  @Override
  public String toString(){
    return where.toString();
  }
}
