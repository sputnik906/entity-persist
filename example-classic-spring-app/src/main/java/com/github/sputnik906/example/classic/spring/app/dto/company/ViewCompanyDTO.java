package com.github.sputnik906.example.classic.spring.app.dto.company;

import com.github.sputnik906.example.classic.spring.app.controller.common.IdLabel;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Location;
import java.util.Set;
import lombok.Value;

@Value
public class ViewCompanyDTO {
  Long id;
  Long version;
  String label;
  Location location;
  Set<IdLabel> departments;
}
