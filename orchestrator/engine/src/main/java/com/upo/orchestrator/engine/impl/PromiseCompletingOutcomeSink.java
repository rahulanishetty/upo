/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.upo.orchestrator.engine.ProcessOutcome;
import com.upo.orchestrator.engine.ProcessOutcomeSink;
import com.upo.orchestrator.engine.models.ProcessInstance;

/**
 * A sink that completes a provided future with the process outcome. Used for synchronous process
 * execution where caller awaits the outcome.
 */
public class PromiseCompletingOutcomeSink extends ProcessOutcomeSink {

  private final CompletableFuture<ProcessOutcome> promise;

  public PromiseCompletingOutcomeSink(CompletableFuture<ProcessOutcome> promise) {
    super("PROMISE");
    this.promise = Objects.requireNonNull(promise, "Promise cannot be null");
  }

  @Override
  public void onOutcome(ProcessInstance processInstance, ProcessOutcome outcome) {
    promise.complete(outcome);
  }

  @Override
  public boolean isSerializable() {
    return false;
  }
}
