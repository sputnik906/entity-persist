package com.github.sputnik906.example.classic.spring.app.dao;

import com.github.sputnik906.example.classic.spring.app.domain.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LocationRepository extends JpaRepository<Location, Long>,
  JpaSpecificationExecutor<Location> {}
