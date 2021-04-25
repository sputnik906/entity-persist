package com.github.sputnik906.example.classic.spring.app.domain.entity;

import com.github.sputnik906.example.classic.spring.app.domain.common.IdentifiableLong;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import lombok.experimental.FieldNameConstants;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@FieldNameConstants
@ToString(callSuper = true)
@Getter
@Entity
public class Employee extends IdentifiableLong {

  @NonNull @NotNull @Setter private String label;

  @Exclude @ManyToMany @NonNull @NotNull private Set<Skill> skills;

  @Exclude
  @OneToOne(fetch = FetchType.LAZY)
  @NonNull
  @NotNull
  private Department department;

  public Set<Skill> getSkills() {
    return Collections.unmodifiableSet(skills);
  }

  public boolean addSkills(Collection<Skill> skills) {
    return this.skills.addAll(skills);
  }

  public boolean removeSkills(Collection<Skill> skills) {
    return this.skills.removeAll(skills);
  }

  public void clearSkills() {
    skills.clear();
  }
}
