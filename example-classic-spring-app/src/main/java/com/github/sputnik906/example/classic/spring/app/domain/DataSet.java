package com.github.sputnik906.example.classic.spring.app.domain;

import com.github.sputnik906.example.classic.spring.app.domain.common.DatasetUtils;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Company;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Department;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Employee;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Location;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Skill;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DataSet {
  public static Map<Class<?>, List<?>> getDev() {
    Map<Class<?>, List<?>> dataSet = new LinkedHashMap<>();

    List<Skill> skills =
        Arrays.asList(new Skill("Skill 1"), new Skill("Skill 2"), new Skill("Skill 3"));

    dataSet.put(Skill.class, skills);

    List<Company> companies =
        DatasetUtils.rangeGeneratorAsList(
            1,
            4,
            i ->
                new Company(
                    "Company " + i,
                    new Location("Location company " + i),
                    new HashSet<>(
                        Arrays.asList(
                            new Department("Department 1 company " + i, 1000.0),
                            new Department("Department 2 company " + i, 1000.0),
                            new Department("Department 3 company " + i, 1000.0)))));

    dataSet.put(Company.class, companies);

    AtomicInteger counter = new AtomicInteger(1);
    List<Employee> employees =
        companies.stream()
            .flatMap(company -> company.getDepartments().stream())
            .map(
                department ->
                    new Employee(
                        "Employee " + counter.get() + " " + department.getLabel(),
                        skills.stream()
                            .limit(counter.getAndSet((counter.get() + 1) % skills.size()))
                            .collect(Collectors.toSet()),
                        department))
            .collect(Collectors.toList());

    dataSet.put(Employee.class, employees);

    return dataSet;
  }
}
