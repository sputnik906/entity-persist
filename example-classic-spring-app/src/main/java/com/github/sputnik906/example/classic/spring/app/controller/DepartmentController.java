package com.github.sputnik906.example.classic.spring.app.controller;

import com.github.sputnik906.example.classic.spring.app.dao.DepartmentService;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Department;
import com.github.sputnik906.example.classic.spring.app.dto.department.DepartmentMapper;
import com.github.sputnik906.example.classic.spring.app.dto.department.ViewDepartmentDTO;
import com.turkraft.springfilter.FilterParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.links.LinkParameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DepartmentController.PATH)
public class DepartmentController {

  public static final String PATH = "api/departments";

  public static final String findAllByIdOperationId = "Department_findAllById";

  private final DepartmentService service;

  private final DepartmentMapper mapper;

  @Autowired
  public DepartmentController(
    DepartmentService service,
    DepartmentMapper mapper
    ) {
    this.service = service;
    this.mapper = mapper;
  }


  @Operation(operationId = findAllByIdOperationId)
  @RequestMapping(value = "/ids",method = RequestMethod.GET,params = "ids")
  public List<ViewDepartmentDTO> findAllById(
    @RequestParam(value = "ids") Long[] ids
  ) {
    return mapper.fromEntities(service.getRepository().findAllById(Arrays.asList(ids)));
  }

  @Operation(
    responses = {
      @ApiResponse(links = {
        @Link(name = Department.Fields.company, operationId = CompanyController.findByIdOperationId, parameters = {
          @LinkParameter(name = "id", expression = "$."+Department.Fields.company+".id")
        })
      })
    })
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public Optional<ViewDepartmentDTO> findById(
    @PathVariable(value = "id") Long id
  ){
    return service.getRepository().findById(id).map(mapper::from);
  }

  @RequestMapping(value = "/sumNominalLoad",method = RequestMethod.GET)
  public Double sumNominalLoad(
    @RequestParam(value = "search", required = false) String search
  ) {
    return service.sumNominalLoad(
      search!=null?FilterParser.parse(search.trim()):null
    );
  }

  @RequestMapping(value = "/maxNominalLoad",method = RequestMethod.GET)
  public Double maxNominalLoad(
    @RequestParam(value = "search", required = false) String search
  ) {
    return service.maxNominalLoad(
      search!=null?FilterParser.parse(search.trim()):null
    );
  }

  @RequestMapping(value = "/minNominalLoad",method = RequestMethod.GET)
  public Double minNominalLoad(
    @RequestParam(value = "search", required = false) String search
  ) {
    return service.minNominalLoad(
      search!=null?FilterParser.parse(search.trim()):null
    );
  }
}
