package unit;

import domain.Address;
import domain.Employee;
import domain.Position;
import domain.Skill;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.github.sputnik906.lang.utils.BeanMapper;

public class BeanMapperTest {

  @Test
  public void convertTest(){

    Employee employee = buildEmployee();

    EmployeeDto employeeDto = BeanMapper.convert(employee,Employee.class,EmployeeDto.class);

    Assertions.assertEquals(employee.getId(),employeeDto.getId());
  }

  @Test
  public void convertMapTest(){

    Employee employee = buildEmployee();

    Map<String,Object> employeeMap = BeanMapper.convert(
      employee,
      Employee.class,
      new String[]{"id","latDeg","lonDeg","address","addr.code","addr.st","attr.attribute1"}
    );

    Assertions.assertEquals(employee.getId(),employeeMap.get("id"));
  }

  private Employee buildEmployee(){
    Skill skill1 = new Skill("1","Skill 1");
    Skill skill2 = new Skill("2","Skill 2");

    Employee employee =
      new Employee(
        "1",
        "Employee 1",
        new HashSet<>(Arrays.asList(skill1, skill2)),
        new Address("Street 1", 443029),
      30);

    employee.getAttributes().put("attribute1","value1");
    employee.getPhones().add("111-11-11");
    employee.setPosition(new Position(Math.toRadians(10),Math.toRadians(10)));

    return employee;
  }


  @Value
  public static class EmployeeDto{
    String id;
    String label;
    Address address;
    AddressDto addr;
    String street;
    Set<SkillDto> skills;
    List<String> phones;
    double latDeg;
    double lonDeg;
  }

  @Value
  public static class AddressDto{
    String st;
    int code;
  }

  @Value
  public static class SkillDto{
    String id;
    String label;
  }

}
