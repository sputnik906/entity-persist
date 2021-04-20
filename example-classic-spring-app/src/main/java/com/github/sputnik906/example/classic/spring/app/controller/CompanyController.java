package com.github.sputnik906.example.classic.spring.app.controller;

import com.github.sputnik906.example.classic.spring.app.controller.common.IdLabel;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Company;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Company.Fields;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Department;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Location;
import com.github.sputnik906.example.classic.spring.app.repository.CompanyRepository;
import com.github.sputnik906.lang.utils.BeanUtils;
import com.turkraft.springfilter.FilterParser;
import com.turkraft.springfilter.FilterSpecification;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/companies")
public class CompanyController {

  private final CompanyRepository repository;

  private final PlatformTransactionManager transactionManager;

  private final TransactionTemplate transactionTemplate;

  @Autowired
  public CompanyController(
    CompanyRepository repository,
    PlatformTransactionManager transactionManager) {
    this.repository = repository;
    this.transactionManager = transactionManager;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
  }

  @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
  public ViewCompanyDTO create(@RequestBody @Valid Company.CreateCompanyDTO dto) {
    Company company = repository.save(Company.from(dto));
    return ViewCompanyDTO.from(company);
  }

  @RequestMapping(value = "/batch", method = RequestMethod.POST, consumes = "application/json")
  public List<ViewCompanyDTO> createBatch(@RequestBody List<Company.CreateCompanyDTO> dtos) {
    List<Company> companies = repository.saveAll(dtos.stream().map(Company::from).collect(Collectors.toList()));
    return ViewCompanyDTO.from(companies);
  }

  @RequestMapping(method = RequestMethod.GET)
  public Page<ViewCompanyDTO> findAll(Pageable pageable){
    Page<Company> companiesPage = repository.findAll(pageable);
    return companiesPage.map(ViewCompanyDTO::from);
  }

  @RequestMapping(method = RequestMethod.GET,params = {"search"})
  public Page<ViewCompanyDTO> search(
    @RequestParam(value = "search") String search,
    Pageable pageable
  ){
    Specification<Company> spec = new FilterSpecification<>(FilterParser.parse(search.trim()));
    Page<Company> companiesPage = repository.findAll(spec,pageable);
    return companiesPage.map(ViewCompanyDTO::from);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public Optional<ViewCompanyDTO> findById(
    @PathVariable(value = "id") Long id
  ){
    return repository.findById(id).map(ViewCompanyDTO::from);
  }

  @RequestMapping(value = "/ids",method = RequestMethod.GET,params = "ids")
  public List<ViewCompanyDTO> findAllById(
    @RequestParam(value = "ids") Long[] ids
  ) {
    return ViewCompanyDTO.from(repository.findAllById(Arrays.asList(ids)));
  }

  @RequestMapping(value = "/{id}/"+ Fields.label, method = RequestMethod.GET)
  public Optional<String> readLabel(
    @PathVariable(value = "id") Long id) {
    return repository.findById(id).map(Company::getLabel);
  }

  @RequestMapping(value = "/{id}/"+ Fields.location, method = RequestMethod.GET)
  public Optional<Location> readLocation(
    @PathVariable(value = "id") Long id) {
    return repository.findById(id).map(Company::getLocation);
  }

  @RequestMapping(value = "/{id}/"+ Fields.departments, method = RequestMethod.GET)
  public Optional<Set<ViewDepartmentDTO>> readDepartments(
    @PathVariable(value = "id") Long id) {
    return repository.findById(id)
      .map(Company::getDepartments)
      .map(ViewDepartmentDTO::from);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
  public ViewCompanyDTO patch(
    @PathVariable(value = "id") Long id,
    @RequestBody Map<Company.MutableProp, Object> patch
  ) {
    return transactionTemplate.execute(status -> {
      Company company = repository.findById(id).orElseThrow(EntityNotFoundException::new);
      patch.forEach((k,v)-> BeanUtils.setProperty(company,k.name(),v));
      return ViewCompanyDTO.from(company);
    });


  }




  @Value
  public static class ViewCompanyDTO{
    Long id;
    Long version;
    String label;
    Location location;
    Set<IdLabel> departments;

    public static ViewCompanyDTO from(Company company){
      return new ViewCompanyDTO(
        company.getId(),
        company.getVersion(),
        company.getLabel(),
        company.getLocation(),
        company.getDepartments().stream()
          .map(d->new IdLabel(d.getId(),d.getLabel()))
          .collect(Collectors.toSet())
      );
    }
    public static List<ViewCompanyDTO> from(List<Company> companies){
      return companies.stream().map(ViewCompanyDTO::from).collect(Collectors.toList());
    }
  }

  @Value
  public static class ViewDepartmentDTO{
    Long id;
    Long version;
    String label;
    Double nominalLoad;

    public static ViewDepartmentDTO from(Department department){
      return new ViewDepartmentDTO(
        department.getId(),
        department.getVersion(),
        department.getLabel(),
        department.getNominalLoad()
      );
    }

    public static Set<ViewDepartmentDTO> from(Set<Department> departments){
      return departments.stream().map(ViewDepartmentDTO::from).collect(Collectors.toSet());
    }
  }
}
