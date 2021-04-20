package com.github.sputnik906.example.classic.spring.app.domain;

import com.github.sputnik906.example.classic.spring.app.domain.common.DatasetUtils;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Company;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Department;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Location;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DataSet {
  public static Map<Class<?>, List<?>> getDev() {
    Map<Class<?>, List<?>> dataSet = new LinkedHashMap<>();

    List<Company> companies =
        DatasetUtils.rangeGeneratorAsList(
            1,
            4,
            i ->
                new Company(
                    "Предприятие " + i,
                    new Location("Локация предприятия " + i),
                    new HashSet<>(
                        Arrays.asList(
                            new Department("Подразделение 1 предприятия " + i, 1000.0),
                            new Department("Подразделение 2 предприятия " + i, 1000.0),
                            new Department("Подразделение 3 предприятия " + i, 1000.0)))));

    dataSet.put(Company.class, companies);

    return dataSet;
  }
}
