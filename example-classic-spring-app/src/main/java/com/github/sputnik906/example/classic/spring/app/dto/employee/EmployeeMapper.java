package com.github.sputnik906.example.classic.spring.app.dto.employee;

import com.github.sputnik906.example.classic.spring.app.domain.entity.Employee;
import com.github.sputnik906.example.classic.spring.app.dto.common.EntityManagerContext;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeMapper extends EntityManagerContext {
  ViewEmployeeDTO from(Employee employee);

  List<ViewEmployeeDTO> fromEntities(Collection<Employee> employees);

  Employee from(CreateEmployeeDTO createEmployeeDTO, @Context EntityManager entityManager);

  List<Employee> fromDTO(
      Collection<CreateEmployeeDTO> employeeDTOList, @Context EntityManager entityManager);

}
