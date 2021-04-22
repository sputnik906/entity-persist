package integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import com.github.sputnik906.example.classic.spring.app.Application;
import com.github.sputnik906.example.classic.spring.app.controller.CompanyController;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Company.Fields;
import com.github.sputnik906.example.classic.spring.app.dto.company.CreateCompanyDTO;
import com.github.sputnik906.example.classic.spring.app.dto.company.ViewCompanyDTO;
import com.github.sputnik906.example.classic.spring.app.dto.department.CreateDepartmentDTO;
import com.github.sputnik906.example.classic.spring.app.dto.department.ViewDepartmentDTO;
import com.github.sputnik906.example.classic.spring.app.dto.location.CreateLocationDTO;
import integration.common.Maps;
import integration.common.RestAssuredEntityApiClient;
import io.restassured.mapper.TypeRef;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,classes = Application.class)
public class BaseIntegrationTest {

  private static RestAssuredEntityApiClient companyClient;

  @BeforeAll
  static void beforeAll(@LocalServerPort Integer port ){
    companyClient =  new RestAssuredEntityApiClient(
      "http://localhost:"+port+"/"+CompanyController.PATH,
      "id",
      "version"
    );
  }

  @Test
  void createCompanyAndDepartmentsAndChangeThem(){
    CreateCompanyDTO companyDTO = new CreateCompanyDTO(
      "Компания 1",
      new CreateLocationDTO("Местоположение компании 1"),
      new HashSet<>()
    );

    ViewCompanyDTO createdCompany =  companyClient.create(
      companyDTO,
      ViewCompanyDTO.class
    );

    List<CreateDepartmentDTO> newDepartments = Arrays.asList(
      new CreateDepartmentDTO("Департамент 1 Компании 1",5000.0),
      new CreateDepartmentDTO("Департамент 2 Компании 1",5000.0)
    );

    List<ViewDepartmentDTO> createdDepartments = companyClient.addToCollection(
      createdCompany.getId().toString(),
      Fields.departments,
      newDepartments,
      new TypeRef<List<ViewDepartmentDTO>>(){}
    );

    companyClient.removeFromCollection(
      createdCompany.getId().toString(),
      Fields.departments,
      Collections.singletonList(createdDepartments.get(1).getId())
    );

    ViewCompanyDTO viewCompanyDTO = companyClient.findById(
      createdCompany.getId().toString(),
      ViewCompanyDTO.class
    );

    assertThat(viewCompanyDTO.getDepartments().size(),equalTo(1));

    Set<ViewDepartmentDTO> departmentDTOS = companyClient.readProperty(
      createdCompany.getId().toString(),
      Fields.departments,
      new TypeRef<Set<ViewDepartmentDTO>>(){}
    );

    assertThat(
      viewCompanyDTO.getDepartments().size(),
      equalTo(departmentDTOS.size())
    );

    companyClient.clearCollection(
      createdCompany.getId().toString(),
      Fields.departments
    );

    Set<ViewDepartmentDTO> departmentDTOSAfterClear = companyClient.readProperty(
      createdCompany.getId().toString(),
      Fields.departments,
      new TypeRef<Set<ViewDepartmentDTO>>(){}
    );

    assertThat(
      departmentDTOSAfterClear.size(),
      equalTo(0)
    );

    ViewCompanyDTO companyAfterPatch = companyClient.patch(
      createdCompany.getId().toString(),
      Maps.of(Fields.label,"Компания 1-1"),
      ViewCompanyDTO.class
    );

    assertThat(
      "Компания 1-1",
      equalTo(companyAfterPatch.getLabel())
    );

    String getCompanyLabel = companyClient.readProperty(
      createdCompany.getId().toString(),
      Fields.label,
      new TypeRef<String>(){}
    );

    companyClient.delete(createdCompany.getId().toString());


    List<ViewCompanyDTO> companyDTOList = companyClient.findAllPaged(
      null,
      ViewCompanyDTO.class
    );

    assertThat(
      companyDTOList.size(),
      equalTo(0)
    );

    Long currentCountCompanies = companyClient.count(null);

    assertThat(
      currentCountCompanies,
      equalTo(0L)
    );

  }



}
