package com.github.sputnik906.persist.api.repository.typed;

import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;

public interface ReadOnlyRepository<ID,DOMAIN> {
  Optional<DOMAIN> findById(ID id);

  Optional<DOMAIN> findById(ID id, LockModeType lockModeType);

  <DTO> Optional<DTO> findById(ID id, Class<DTO> dto);

  List<DOMAIN> findAllById(Iterable<ID> ids);

  List<DOMAIN> findAll();

  <DTO> List<DTO> findAll(Class<DTO> dto);

  DOMAIN getReference(ID id);
}
