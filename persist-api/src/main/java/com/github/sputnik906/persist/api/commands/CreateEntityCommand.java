package com.github.sputnik906.persist.api.commands;

import lombok.Value;

@Value
public class CreateEntityCommand<CreateDto> {
  String anchorId; //Для последующей ссылки на созданную сущность
  String commandType = "Create";
  String entityName;
  CreateDto params;
}
