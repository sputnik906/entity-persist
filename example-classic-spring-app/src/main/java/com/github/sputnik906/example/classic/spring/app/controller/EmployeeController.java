package com.github.sputnik906.example.classic.spring.app.controller;

import com.github.sputnik906.example.classic.spring.app.dao.EmployeeService;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Department;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Skill;
import com.github.sputnik906.example.classic.spring.app.dto.employee.CreateEmployeeDTO;
import com.github.sputnik906.example.classic.spring.app.dto.employee.CreateEmployeeDTO.Fields;
import com.github.sputnik906.example.classic.spring.app.dto.employee.EmployeeMapper;
import com.github.sputnik906.example.classic.spring.app.dto.employee.ViewEmployeeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(EmployeeController.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmployeeController {

  public static final String PATH = "api/employees";

  private final EmployeeService service;

  private final EmployeeMapper mapper;

  @Operation(requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(extensions = {
    @Extension(name="x-links", properties = {
      @ExtensionProperty(name = Fields.skills, value = ""
        + "{\"operationId\":\""+SkillController.searchOperationId+"\","
        + "\"transform\":{\""+ "id"+"\":\"$.id\"},"
        + "\"label\":\"$."+ Skill.Fields.label +"\""
        + "}", parseValue = true),
      @ExtensionProperty(name = Fields.department, value = ""
        + "{\"operationId\":\""+DepartmentController.searchOperationId+"\","
        + "\"transform\":{\""+ "id"+"\":\"$.id\"},"
        + "\"label\":\"$."+ Department.Fields.label +"\""
        + "}", parseValue = true)
    })
  }))
  @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
  public ViewEmployeeDTO create(
    @RequestBody @Valid CreateEmployeeDTO dto
  ) {
    return mapper.from(service.create(dto));
  }

  @RequestMapping(value = "/batch", method = RequestMethod.POST, consumes = "application/json")
  public List<ViewEmployeeDTO> createBatch(
    @RequestBody List<CreateEmployeeDTO> dtoList
  ) {
    return mapper.fromEntities(service.createBatch(dtoList));
  }
}
