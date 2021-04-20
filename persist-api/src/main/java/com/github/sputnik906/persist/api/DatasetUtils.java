package com.github.sputnik906.persist.api;

import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DatasetUtils {
  public static <D> List<D> rangeGeneratorAsList(int startNumber, int count,
    IntFunction<? extends D> mapFun){
    return IntStream.range(startNumber, startNumber + count)
      .mapToObj(mapFun)
      .collect(Collectors.toList());
  }
  public static <D> Set<D> rangeGeneratorAsSet(int startNumber, int count,
    IntFunction<? extends D> mapFun){
    return IntStream.range(startNumber, startNumber + count)
      .mapToObj(mapFun)
      .collect(Collectors.toSet());
  }
}
