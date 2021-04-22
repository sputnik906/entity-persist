package integration.common;

import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Maps {
  public static Map<String, Object> of(Object... objs) {
    Map<String, Object> map = new HashMap<>();
    for (int i = 0; i < objs.length; i += 2) map.put((String) objs[i], objs[i + 1]);
    return map;
  }
}
