package com.github.sputnik906.example.classic.spring.app.dto.department;

import com.github.sputnik906.example.classic.spring.app.domain.entity.Department;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {
  ViewDepartmentDTO from(Department department);

  List<ViewDepartmentDTO> fromEntities(Collection<Department> departments);

  Department from(CreateDepartmentDTO createDepartmentDTO);

  List<Department> fromDTO(Collection<CreateDepartmentDTO> departmentDTOList);
}
