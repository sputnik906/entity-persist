package com.github.sputnik906.example.classic.spring.app.controller;

import com.github.sputnik906.example.classic.spring.app.dao.SkillRepository;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Skill;
import com.github.sputnik906.example.classic.spring.app.dto.skill.CreateSkillDTO;
import com.github.sputnik906.example.classic.spring.app.dto.skill.SkillMapper;
import com.turkraft.springfilter.FilterParser;
import com.turkraft.springfilter.FilterSpecification;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(SkillController.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SkillController {

  public static final String PATH = "api/skills";

  public static final String searchOperationId = "Skill_search";

  private final SkillRepository repository;

  private final SkillMapper mapper;

  @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
  public Skill create(
    @RequestBody @Valid CreateSkillDTO dto
  ) {
    return repository.save(mapper.from(dto));
  }

  @RequestMapping(value = "/batch", method = RequestMethod.POST, consumes = "application/json")
  public List<Skill> createBatch(
    @RequestBody List<CreateSkillDTO> dtoList
  ) {
    return repository.saveAll(mapper.fromDTO(dtoList));
  }

  @RequestMapping(method = RequestMethod.GET)
  public Page<Skill> findAll(Pageable pageable){
    return repository.findAll(pageable);
  }

  @Operation(operationId = SkillController.searchOperationId)
  @RequestMapping(method = RequestMethod.GET,params = "search")
  public Page<Skill> search(
    @RequestParam(value = "search") String search,
    Pageable pageable
  ){
    return repository.findAll(
      new FilterSpecification<>(FilterParser.parse(search.trim())),
      pageable
    );
  }
}
