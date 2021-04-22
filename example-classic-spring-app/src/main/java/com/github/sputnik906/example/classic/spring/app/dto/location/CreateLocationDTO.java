package com.github.sputnik906.example.classic.spring.app.dto.location;

import javax.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class CreateLocationDTO {
  @NotBlank String label;
}
