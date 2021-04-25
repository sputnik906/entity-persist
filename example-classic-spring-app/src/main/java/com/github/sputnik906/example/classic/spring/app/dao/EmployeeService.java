package com.github.sputnik906.example.classic.spring.app.dao;

import com.github.sputnik906.example.classic.spring.app.domain.entity.Employee;
import com.github.sputnik906.example.classic.spring.app.dto.employee.CreateEmployeeDTO;
import com.github.sputnik906.example.classic.spring.app.dto.employee.EmployeeMapper;
import java.util.List;
import javax.persistence.EntityManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmployeeService {
  @Getter
  private final EmployeeRepository repository;

  private final EntityManager entityManager;

  private final EmployeeMapper mapper;

  public Employee create(CreateEmployeeDTO createEmployeeDTO){
    return repository.save(mapper.from(createEmployeeDTO,entityManager));
  }

  public List<Employee> createBatch(List<CreateEmployeeDTO> createEmployeeDTOs){
    return repository.saveAll(mapper.fromDTO(createEmployeeDTOs,entityManager));
  }


}
