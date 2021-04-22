package com.github.sputnik906.example.classic.spring.app.domain.entity;

import com.github.sputnik906.example.classic.spring.app.domain.common.IdentifiableLong;
import io.swagger.v3.oas.annotations.media.Schema;
import java.beans.ConstructorProperties;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import lombok.experimental.FieldNameConstants;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldNameConstants
@ToString(callSuper = true)
@Getter
@Entity
public class Company extends IdentifiableLong {

  @ConstructorProperties({Fields.label, Fields.location, Fields.departments})
  public Company(String label, Location location, Set<Department> departments) {
    this.label = label;
    this.location = location;
    this.departments = new HashSet<>();
    addDepartments(departments);

    Validation.buildDefaultValidatorFactory().getValidator().validate(this);
  }

  /** Наименование */
  @NotBlank
  @NonNull
  @Setter
  @Schema(title = "Наименование")
  private String label;

  /** Местоположение */
  @OneToOne(cascade = CascadeType.ALL)
  @NotNull
  @Schema(title = "Местоположение")
  private Location location;

  /** Подразделения */
  @Exclude
  @NotNull
  @NonNull
  @OneToMany(mappedBy = Department.Fields.company, cascade = CascadeType.ALL, orphanRemoval = true)
  @Schema(title = "Подразделения")
  private Set<@Valid Department> departments;

  public Set<Department> getDepartments() {
    return Collections.unmodifiableSet(departments);
  }

  public boolean addDepartments(Collection<Department> newDepartments) {
    newDepartments.stream()
        .filter(d -> d.getCompany() != null)
        .findFirst()
        .ifPresent(
            d -> {
              throw new IllegalArgumentException("Department has belong Company already ");
            });
    newDepartments.forEach(d -> d.setCompany(this));
    return departments.addAll(newDepartments);
  }

  public boolean removeDepartments(Collection<Department> removeDepartments) {
    return departments.removeAll(removeDepartments);
  }

  public void clearDepartments(){
    departments.clear();
  }


}
