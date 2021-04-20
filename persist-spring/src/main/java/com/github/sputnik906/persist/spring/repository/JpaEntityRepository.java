package com.github.sputnik906.persist.spring.repository;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface JpaEntityRepository<ID,DOMAIN> extends JpaRepository<DOMAIN, ID>,
    JpaSpecificationExecutor<DOMAIN> {

  <DTO> Collection<DTO> findAllBy(Class<DTO> type);

  <DTO> Optional<DTO> findById(ID id, Class<DTO> dto);

}
