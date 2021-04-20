package com.github.sputnik906.example.classic.spring.app.controller.common;


import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.Value;

@Value
public class IdLabel {
  @NonNull
  @NotNull
  Long id;
  String label;
}
