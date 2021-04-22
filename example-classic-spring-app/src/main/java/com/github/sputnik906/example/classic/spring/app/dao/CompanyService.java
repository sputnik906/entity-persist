package com.github.sputnik906.example.classic.spring.app.dao;

import com.github.sputnik906.example.classic.spring.app.domain.entity.Company;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Department;
import com.github.sputnik906.example.classic.spring.app.dto.department.CreateDepartmentDTO;
import com.github.sputnik906.example.classic.spring.app.dto.department.DepartmentMapper;
import com.github.sputnik906.lang.utils.BeanUtils;
import com.turkraft.springfilter.FilterSpecification;
import com.turkraft.springfilter.node.Filter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityNotFoundException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyService {

  private final String idFieldName = "id";
  private final String versionFieldName = "version";

  @Getter
  private final CompanyRepository repository;

  private final DepartmentRepository departmentRepository;

  private final DepartmentMapper departmentMapper;

  @Autowired
  public CompanyService(
    CompanyRepository repository,
    DepartmentRepository departmentRepository,
    DepartmentMapper departmentMapper) {
    this.repository = repository;
    this.departmentRepository = departmentRepository;
    this.departmentMapper = departmentMapper;
  }

  @Transactional(readOnly = true)
  public Page<Company> search(Pageable pageable, Filter search){
   return repository.findAll(
      search!=null
        ?new FilterSpecification<>(search)
        :null,
      pageable
    );
  }

  @Transactional(readOnly = true)
  public Page<Company> paged(Pageable pageable){
    return search(pageable,null);
  }

  @Transactional(readOnly = true)
  public Long count(Filter search){
    if (search!=null) return repository.count(new FilterSpecification<>(search));
    return repository.count();
  }

  public List<Department> addDepartments(Long id,List<CreateDepartmentDTO> createDepartmentDTOS){
    Company company = repository.findById(id).orElseThrow(EntityNotFoundException::new);
    List<Department> newDepartments = createDepartmentDTOS.stream().map(departmentMapper::from).collect(Collectors.toList());
    company.addDepartments(newDepartments);
    return newDepartments;
  }

  public void removeDepartments(Long id,Long[] departmentIds){
    Company company = repository.findById(id).orElseThrow(EntityNotFoundException::new);
    List<Department> departments = Stream.of(departmentIds)
      .map(departmentRepository::getOne)
      .collect(Collectors.toList());
    company.removeDepartments(departments);
  }

  public void clearDepartments(Long id){
    Company company = repository.findById(id).orElseThrow(EntityNotFoundException::new);
    company.clearDepartments();
  }

  public List<Company> patch(List<Map<String, Object>> patches){
    List<Long> ids = patches.stream()
      .mapToLong(m->(Long)m.get(idFieldName))
      .boxed()
      .collect(Collectors.toList());

    List<Company> companies = repository.findAllById(ids);

    Map<Long,Map<String, Object>> idPatchMap = patches.stream().collect(Collectors.toMap(m->(Long)m.get(idFieldName),m->m));

    companies.forEach(company ->
      patch(company,idPatchMap.get(company.getId()))
    );

    return companies;
  }

  public Company patch(Long id,Map<String, Object> patch){
    Company company = repository.findById(id).orElseThrow(EntityNotFoundException::new);
    return patch(company,patch);
  }

  private Company patch(Company company,Map<String, Object> patch){
    if (patch.containsKey(versionFieldName)&&!company.getVersion().equals(patch.get(versionFieldName)))
      throw new IllegalStateException("Wrong version");
    patch.remove(versionFieldName);
    patch.remove(idFieldName);
    patch.forEach((k,v)-> BeanUtils.setProperty(company,k,v));
    return company;
  }

  public void delete(Long[] ids){
    List<Company> companies = Stream.of(ids)
      .map(repository::getOne)
      .collect(Collectors.toList());
    repository.deleteAll(companies);
  }






}
