package com.github.sputnik906.example.classic.spring.app.dto.employee;

import java.util.Set;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@FieldNameConstants
public class CreateEmployeeDTO {
  String label;
  Set<Long> skills;
  Long department;
}
