package com.github.sputnik906.example.classic.spring.app.dao;

import com.github.sputnik906.example.classic.spring.app.domain.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SkillRepository extends JpaRepository<Skill, Long>,
  JpaSpecificationExecutor<Skill> {

}
