package com.github.sputnik906.lang.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Maps {
  public static Map<String, Object> of(Object... objs) {
    Map<String, Object> map = new HashMap<>();
    for (int i = 0; i < objs.length; i += 2) map.put((String) objs[i], objs[i + 1]);
    return map;
  }

  public static Map<String, Object> addPrefixToKey(String prefix,Map<String, Object> map){
    if (map==null) return null;

    Map<String, Object> result = new HashMap<>();

    for(Entry<String, Object> entry : map.entrySet()){
      result.put(prefix+entry.getKey(), entry.getValue());
    }

    return result;
  }
}
