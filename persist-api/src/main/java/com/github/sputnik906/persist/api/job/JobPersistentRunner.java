package com.github.sputnik906.persist.api.job;

import java.util.concurrent.CompletableFuture;

public interface JobPersistentRunner {

  CompletableFuture<JobInfo> runJobBeforeCommit(String jobName,Object params);
}
