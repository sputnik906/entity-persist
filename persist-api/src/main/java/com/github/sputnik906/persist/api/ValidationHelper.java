package com.github.sputnik906.persist.api;

import javax.validation.Validation;
import javax.validation.Validator;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationHelper {
  public static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
}
