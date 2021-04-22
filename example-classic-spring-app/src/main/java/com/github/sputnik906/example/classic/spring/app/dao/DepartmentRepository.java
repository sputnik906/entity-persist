package com.github.sputnik906.example.classic.spring.app.dao;

import com.github.sputnik906.example.classic.spring.app.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepartmentRepository extends JpaRepository<Department, Long> ,
  JpaSpecificationExecutor<Department> {}
