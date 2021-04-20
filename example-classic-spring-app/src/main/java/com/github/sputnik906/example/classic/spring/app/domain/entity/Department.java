package com.github.sputnik906.example.classic.spring.app.domain.entity;

import com.github.sputnik906.example.classic.spring.app.domain.common.IdentifiableLong;
import java.util.Collections;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.PreRemove;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@FieldNameConstants
@ToString(callSuper = true)
@Getter
@Entity
public class Department extends IdentifiableLong {

  /** Наименование */
  @NotBlank @NonNull @Setter private String label;

  /** Номинальная загрузка */
  @NotNull @NonNull @Positive private Double nominalLoad;

  // Не позволит прямой persist, только через Компанию
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @Setter(AccessLevel.PROTECTED)
  private Company company;

  @PreRemove
  public void remove() {
    company.removeDepartments(Collections.singleton(this));
  }

  public static Department from(CreateDepartmentDTO dto){
    return new Department(dto.label,dto.nominalLoad);
  }

  @Value
  public static class CreateDepartmentDTO {
    @NotBlank String label;

    @Positive Double nominalLoad;
  }
}
