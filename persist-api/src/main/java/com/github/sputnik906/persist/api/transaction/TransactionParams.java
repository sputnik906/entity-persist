package com.github.sputnik906.persist.api.transaction;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TransactionParams {
  private boolean readOnly = false;
}
