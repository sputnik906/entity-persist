package com.github.sputnik906.example.classic.spring.app.dto.department;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import lombok.Value;

@Value
public class CreateDepartmentDTO {
  @NotBlank String label;

  @Positive Double nominalLoad;
}
