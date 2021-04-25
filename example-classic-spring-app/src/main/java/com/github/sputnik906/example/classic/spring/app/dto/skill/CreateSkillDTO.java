package com.github.sputnik906.example.classic.spring.app.dto.skill;

import javax.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class CreateSkillDTO {
  @NotBlank String label;
}
