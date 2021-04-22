package com.github.sputnik906.example.classic.spring.app.dto.company;

import com.github.sputnik906.example.classic.spring.app.domain.entity.Company;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

  ViewCompanyDTO from(Company company);

  List<ViewCompanyDTO> fromEntities(List<Company> companies);

  Company from(CreateCompanyDTO createCompanyDTO);

  List<Company> fromDTO(List<CreateCompanyDTO> companyDTOList);

}
