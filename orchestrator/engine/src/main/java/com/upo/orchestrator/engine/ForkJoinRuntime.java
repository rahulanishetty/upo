/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import com.upo.orchestrator.engine.models.ProcessInstance;

/** Runtime for managing fork-join process execution patterns. */
public interface ForkJoinRuntime {

  /**
   * Processes completion of a concurrent instance within a fork-join block. Determines if join
   * condition is met and triggers parent process continuation.
   *
   * @param concurrentInstance Completed concurrent instance
   * @param parentInstance Parent process instance waiting for join
   * @param flowStatus Completion status of concurrent instance
   */
  void join(
      ProcessInstance concurrentInstance,
      ProcessInstance parentInstance,
      ProcessFlowStatus flowStatus);
}
