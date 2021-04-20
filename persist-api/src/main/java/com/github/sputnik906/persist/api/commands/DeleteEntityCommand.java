package com.github.sputnik906.persist.api.commands;

import lombok.Value;

@Value
public class DeleteEntityCommand {
  String commandType = "Delete";
  String id;
  String entityName;
}
