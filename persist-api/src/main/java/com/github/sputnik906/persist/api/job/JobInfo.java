package com.github.sputnik906.persist.api.job;

import lombok.NonNull;
import lombok.Value;

@Value
public class JobInfo {
  @NonNull
  Long instanceId;
  @NonNull
  Long executionId;
}
