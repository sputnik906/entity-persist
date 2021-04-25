package com.github.sputnik906.example.classic.spring.app.dto.common;

import com.github.sputnik906.example.classic.spring.app.domain.common.IdentifiableLong;
import javax.persistence.EntityManager;
import org.mapstruct.Context;
import org.mapstruct.TargetType;

public interface EntityManagerContext {

  default <T extends IdentifiableLong> T from(
    Long id, @Context EntityManager entityManager, @TargetType Class<T> targetType) {
    return entityManager.getReference(targetType, id);
  }
}
