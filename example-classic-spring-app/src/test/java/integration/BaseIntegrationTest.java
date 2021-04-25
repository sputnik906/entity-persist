package integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import com.github.sputnik906.example.classic.spring.app.Application;
import com.github.sputnik906.example.classic.spring.app.controller.CompanyController;
import com.github.sputnik906.example.classic.spring.app.controller.EmployeeController;
import com.github.sputnik906.example.classic.spring.app.controller.SkillController;
import com.github.sputnik906.example.classic.spring.app.controller.common.IdLabel;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Company.Fields;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Skill;
import com.github.sputnik906.example.classic.spring.app.dto.company.CreateCompanyDTO;
import com.github.sputnik906.example.classic.spring.app.dto.company.ViewCompanyDTO;
import com.github.sputnik906.example.classic.spring.app.dto.department.CreateDepartmentDTO;
import com.github.sputnik906.example.classic.spring.app.dto.department.ViewDepartmentDTO;
import com.github.sputnik906.example.classic.spring.app.dto.employee.CreateEmployeeDTO;
import com.github.sputnik906.example.classic.spring.app.dto.employee.ViewEmployeeDTO;
import com.github.sputnik906.example.classic.spring.app.dto.location.CreateLocationDTO;
import com.github.sputnik906.example.classic.spring.app.dto.skill.CreateSkillDTO;
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

  private static RestAssuredEntityApiClient client;

  @BeforeAll
  static void beforeAll(@LocalServerPort Integer port ){
    client =  new RestAssuredEntityApiClient(
      "http://localhost:"+port,
      "id",
      "version"
    );
  }

  ViewCompanyDTO createDefaultCompanyWithDepartments(RestAssuredEntityApiClient client){
    CreateCompanyDTO companyDTO = new CreateCompanyDTO(
      "Компания 1",
      new CreateLocationDTO("Местоположение компании 1"),
      new HashSet<>()
    );

    ViewCompanyDTO createdCompany =  client.create(
      CompanyController.PATH,
      companyDTO,
      ViewCompanyDTO.class
    );

    List<CreateDepartmentDTO> newDepartments = Arrays.asList(
      new CreateDepartmentDTO("Департамент 1 Компании 1",5000.0),
      new CreateDepartmentDTO("Департамент 2 Компании 1",5000.0)
    );

    List<ViewDepartmentDTO> createdDepartments = client.addToCollection(
      CompanyController.PATH,
      createdCompany.getId().toString(),
      Fields.departments,
      newDepartments,
      new TypeRef<List<ViewDepartmentDTO>>(){}
    );

    return client.findById(
      CompanyController.PATH,
      createdCompany.getId().toString(),
      ViewCompanyDTO.class
    );
  }

  @Test
  void createCompanyAndDepartmentsAndChangeThem(){

    ViewCompanyDTO createdCompany = createDefaultCompanyWithDepartments(client);

    client.removeFromCollection(
      CompanyController.PATH,
      createdCompany.getId().toString(),
      Fields.departments,
      Collections.singletonList(createdCompany.getDepartments().stream().findFirst().map(
        IdLabel::getId).orElseThrow(IllegalStateException::new))
    );

    ViewCompanyDTO viewCompanyDTO = client.findById(
      CompanyController.PATH,
      createdCompany.getId().toString(),
      ViewCompanyDTO.class
    );

    assertThat(viewCompanyDTO.getDepartments().size(),equalTo(1));

    Set<ViewDepartmentDTO> departmentDTOS = client.readProperty(
      CompanyController.PATH,
      createdCompany.getId().toString(),
      Fields.departments,
      new TypeRef<Set<ViewDepartmentDTO>>(){}
    );

    assertThat(
      viewCompanyDTO.getDepartments().size(),
      equalTo(departmentDTOS.size())
    );

    client.clearCollection(
      CompanyController.PATH,
      createdCompany.getId().toString(),
      Fields.departments
    );

    Set<ViewDepartmentDTO> departmentDTOSAfterClear = client.readProperty(
      CompanyController.PATH,
      createdCompany.getId().toString(),
      Fields.departments,
      new TypeRef<Set<ViewDepartmentDTO>>(){}
    );

    assertThat(
      departmentDTOSAfterClear.size(),
      equalTo(0)
    );

    ViewCompanyDTO companyAfterPatch = client.patch(
      CompanyController.PATH,
      createdCompany.getId().toString(),
      Maps.of(Fields.label,"Компания 1-1"),
      ViewCompanyDTO.class
    );

    assertThat(
      "Компания 1-1",
      equalTo(companyAfterPatch.getLabel())
    );

    String getCompanyLabel = client.readProperty(
      CompanyController.PATH,
      createdCompany.getId().toString(),
      Fields.label,
      new TypeRef<String>(){}
    );

    client.delete(
      CompanyController.PATH,
      createdCompany.getId().toString()
    );


    List<ViewCompanyDTO> companyDTOList = client.findAllPaged(
      CompanyController.PATH,
      null,
      ViewCompanyDTO.class
    );

    assertThat(
      companyDTOList.size(),
      equalTo(0)
    );

    Long currentCountCompanies = client.count(
      CompanyController.PATH,
      null
    );

    assertThat(
      currentCountCompanies,
      equalTo(0L)
    );

  }

  @Test
  void createSkillCompanyEmployee(){

    Skill skill1 = client.create(
      SkillController.PATH,
      new CreateSkillDTO("Skill 1"),
      Skill.class
    );

    Skill skill2 = client.create(
      SkillController.PATH,
      new CreateSkillDTO("Skill 2"),
      Skill.class
    );

    ViewCompanyDTO createdCompany = createDefaultCompanyWithDepartments(client);

    ViewEmployeeDTO viewEmployeeDTO = client.create(
      EmployeeController.PATH,
      new CreateEmployeeDTO(
        "Employee 1",
        new HashSet<>(Arrays.asList(skill1.getId(),skill2.getId())),
        createdCompany.getDepartments().stream()
          .findFirst().orElseThrow(IllegalStateException::new).getId()
      ),
      ViewEmployeeDTO.class
    );

  }



}
