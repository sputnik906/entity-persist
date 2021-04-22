package com.github.sputnik906.example.classic.spring.app.controller;

import com.github.sputnik906.example.classic.spring.app.domain.entity.Company;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Company.Fields;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Location;
import com.github.sputnik906.example.classic.spring.app.dto.company.CompanyMapper;
import com.github.sputnik906.example.classic.spring.app.dto.company.CreateCompanyDTO;
import com.github.sputnik906.example.classic.spring.app.dto.company.ViewCompanyDTO;
import com.github.sputnik906.example.classic.spring.app.dto.department.CreateDepartmentDTO;
import com.github.sputnik906.example.classic.spring.app.dto.department.DepartmentMapper;
import com.github.sputnik906.example.classic.spring.app.dto.department.ViewDepartmentDTO;
import com.github.sputnik906.example.classic.spring.app.dao.CompanyService;
import com.turkraft.springfilter.FilterParser;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CompanyController.PATH)
public class CompanyController {

  public static final String PATH = "api/companies";

  private final CompanyService service;

  private final CompanyMapper companyMapper;

  private final DepartmentMapper departmentMapper;

  @Autowired
  public CompanyController(CompanyService service,
    CompanyMapper companyMapper,
    DepartmentMapper departmentMapper
  ) {
    this.service = service;
    this.companyMapper = companyMapper;
    this.departmentMapper = departmentMapper;
  }

  @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
  public ViewCompanyDTO create(
    @RequestBody @Valid CreateCompanyDTO dto
  ) {
    Company company = service.getRepository().save(companyMapper.from(dto));
    return companyMapper.from(company);
  }

  @RequestMapping(value = "/batch", method = RequestMethod.POST, consumes = "application/json")
  public List<ViewCompanyDTO> createBatch(
    @RequestBody List<CreateCompanyDTO> companyDTOList
  ) {
    List<Company> companies = service.getRepository().saveAll(companyMapper.fromDTO(companyDTOList));
    return companyMapper.fromEntities(companies);
  }

  @RequestMapping(method = RequestMethod.GET)
  public Page<ViewCompanyDTO> findAll(Pageable pageable){
    return service.paged(pageable).map(companyMapper::from);
  }

  @RequestMapping(method = RequestMethod.GET,params = "search")
  public Page<ViewCompanyDTO> search(
    @RequestParam(value = "search", required = false) String search,
    Pageable pageable
  ){
    return service.search(
      pageable,
      search!=null?FilterParser.parse(search.trim()):null
    ).map(companyMapper::from);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public Optional<ViewCompanyDTO> findById(
    @PathVariable(value = "id") Long id
  ){
    return service.getRepository().findById(id).map(companyMapper::from);
  }

  @RequestMapping(value = "/ids",method = RequestMethod.GET,params = "ids")
  public List<ViewCompanyDTO> findAllById(
    @RequestParam(value = "ids") Long[] ids
  ) {
    return companyMapper.fromEntities(service.getRepository().findAllById(Arrays.asList(ids)));
  }

  @RequestMapping(value = "/{id}/"+ Fields.label, method = RequestMethod.GET)
  public Optional<String> readLabel(
    @PathVariable(value = "id") Long id) {
    return service.getRepository().findById(id).map(Company::getLabel);
  }

  @RequestMapping(value = "/{id}/"+ Fields.location, method = RequestMethod.GET)
  public Optional<Location> readLocation(
    @PathVariable(value = "id") Long id) {
    return service.getRepository().findById(id).map(Company::getLocation);
  }

  @RequestMapping(value = "/{id}/"+ Fields.departments, method = RequestMethod.GET)
  public Optional<List<ViewDepartmentDTO>> readDepartments(
    @PathVariable(value = "id") Long id) {
    return service.getRepository().findById(id)
      .map(Company::getDepartments)
      .map(departmentMapper::fromEntities);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
  public ViewCompanyDTO patch(
    @PathVariable(value = "id") Long id,
    @RequestBody Map<String, Object> patch
  ) {
    return companyMapper.from(service.patch(id,patch));
  }

  @RequestMapping(method = RequestMethod.PATCH)
  public List<ViewCompanyDTO> patchBatch(
    @RequestBody List<Map<String, Object>> patches
  ) {
    return service.patch(patches).stream()
      .map(companyMapper::from)
      .collect(Collectors.toList());
  }

  @RequestMapping(value = "/{id}/"+Fields.departments+"/add", method = RequestMethod.POST)
  public List<ViewDepartmentDTO> addDepartments(
    @PathVariable(value = "id") Long id,
    @RequestBody @Valid List<CreateDepartmentDTO> departments
  ) {
    return service.addDepartments(id,departments).stream()
      .map(departmentMapper::from)
      .collect(Collectors.toList());

  }
  @RequestMapping(value = "/{id}/"+Fields.departments+"/remove", method = RequestMethod.DELETE)
  public void removeDepartments(
    @PathVariable(value = "id") Long id,
    @RequestBody @Valid Long[] departmentIds
  ) {
    service.removeDepartments(id,departmentIds);
  }

  @RequestMapping(value = "/{id}/"+Fields.departments+"/clear", method = RequestMethod.DELETE)
  public void clearDepartments(
    @PathVariable(value = "id") Long id
  ) {
    service.clearDepartments(id);
  }

  @RequestMapping(value = "/count",method = RequestMethod.GET)
  public Long count(
    @RequestParam(value = "search", required = false) String search
  ) {
    return service.count(
      search!=null? FilterParser.parse(search.trim()):null
    );
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public void delete(@PathVariable(value = "id") Long id) {
    service.getRepository().deleteById(id);
  }

  @RequestMapping(params = "ids", method = RequestMethod.DELETE)
  public void deleteIds(@RequestParam(value = "ids") Long[] ids) {
    service.delete(ids);
  }


}
