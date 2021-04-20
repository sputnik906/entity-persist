package com.github.sputnik906.persist.api;

import java.util.List;
import lombok.Value;

@Value
public class DataCollectionParam {
  List<?> entities;
  String name;
}
