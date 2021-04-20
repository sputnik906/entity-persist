package com.github.sputnik906.persist.api.commands;

import java.util.Map;
import lombok.Value;

@Value
public class UpdateEntityCommand {
  String commandType = "Update";
  String id;
  String entityName;
  Map<String,Object> props;
}
