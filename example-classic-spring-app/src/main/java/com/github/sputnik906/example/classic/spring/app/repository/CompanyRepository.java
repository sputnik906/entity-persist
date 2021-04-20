package com.github.sputnik906.example.classic.spring.app.repository;

import com.github.sputnik906.example.classic.spring.app.domain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {}
