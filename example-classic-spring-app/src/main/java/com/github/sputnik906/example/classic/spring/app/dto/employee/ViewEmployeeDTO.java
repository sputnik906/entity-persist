package com.github.sputnik906.example.classic.spring.app.dto.employee;

import com.github.sputnik906.example.classic.spring.app.controller.common.IdLabel;
import java.util.Set;
import lombok.Value;

@Value
public class ViewEmployeeDTO {
  Long id;
  Long version;
  String label;
  Set<IdLabel> skills;
  IdLabel department;
}
