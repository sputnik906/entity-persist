package com.github.sputnik906.example.classic.spring.app.dao;

import com.github.sputnik906.example.classic.spring.app.domain.entity.Department;
import com.github.sputnik906.example.classic.spring.app.domain.entity.Department.Fields;
import com.github.sputnik906.example.classic.spring.app.dao.common.JpaUtils;
import com.turkraft.springfilter.FilterSpecification;
import com.turkraft.springfilter.node.Filter;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.SingularAttribute;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DepartmentService {

  @Getter
  private final DepartmentRepository departmentRepository;

  private final EntityManager entityManager;

  final SingularAttribute<Department,Double> nominalLoadColumn;

  @Autowired
  public DepartmentService(
    DepartmentRepository departmentRepository,
    EntityManager entityManager
  ) {
    this.departmentRepository = departmentRepository;
    this.entityManager = entityManager;
    //check column type on Spring bean created stage
    this.nominalLoadColumn = entityManager.getMetamodel()
      .managedType(Department.class)
      .getDeclaredSingularAttribute(Fields.nominalLoad,Double.class);
  }

  public Double sumNominalLoad(Filter search){
    return JpaUtils.sum(
      entityManager,
      search!=null
      ?new FilterSpecification<>(search)
      :null,
      nominalLoadColumn
    );
  }

  public Double maxNominalLoad(Filter search){
    return JpaUtils.max(
      entityManager,
      search!=null
        ?new FilterSpecification<>(search)
        :null,
      nominalLoadColumn
    );
  }

  public Double minNominalLoad(Filter search){
    return JpaUtils.min(
      entityManager,
      search!=null
        ?new FilterSpecification<>(search)
        :null,
      nominalLoadColumn
    );
  }
}
