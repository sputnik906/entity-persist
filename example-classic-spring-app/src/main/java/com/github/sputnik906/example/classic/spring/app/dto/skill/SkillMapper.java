package com.github.sputnik906.example.classic.spring.app.dto.skill;

import com.github.sputnik906.example.classic.spring.app.domain.entity.Skill;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkillMapper {
  Skill from(CreateSkillDTO createSkillDTO);

  List<Skill> fromDTO(List<CreateSkillDTO> skillDTOList);
}
