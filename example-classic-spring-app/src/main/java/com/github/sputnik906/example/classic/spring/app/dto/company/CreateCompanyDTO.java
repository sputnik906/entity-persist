package com.github.sputnik906.example.classic.spring.app.dto.company;

import com.github.sputnik906.example.classic.spring.app.dto.department.CreateDepartmentDTO;
import com.github.sputnik906.example.classic.spring.app.dto.location.CreateLocationDTO;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class CreateCompanyDTO {
  @NotBlank String label;

  @NotNull CreateLocationDTO location;

  @NotNull Set<@Valid CreateDepartmentDTO> departments;
}
