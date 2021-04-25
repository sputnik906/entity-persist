package com.github.sputnik906.example.classic.spring.app.domain.entity;

import com.github.sputnik906.example.classic.spring.app.domain.common.IdentifiableLong;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@FieldNameConstants
@ToString(callSuper = true)
@Getter
@Entity
public class Skill extends IdentifiableLong {

  @NonNull @NotNull private String label;
}
