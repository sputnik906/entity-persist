package com.github.sputnik906.example.classic.spring.app.dto.department;

import com.github.sputnik906.example.classic.spring.app.controller.common.IdLabel;
import lombok.Value;

@Value
public class ViewDepartmentDTO {
  Long id;
  Long version;
  String label;
  Double nominalLoad;
  IdLabel company;
}
