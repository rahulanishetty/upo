/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import com.upo.orchestrator.engine.models.CompletionSignal;
import com.upo.orchestrator.engine.models.ProcessInstance;

import jakarta.validation.ValidationException;

/**
 * Defines the contract for executable task units within the process orchestration system. Each task
 * implementation represents a discrete unit of work that can be executed as part of a process flow.
 */
public interface TaskExecutor {
  /**
   * Executes the core task logic with provided process context and typed inputs.
   *
   * @param context The process instance providing execution context and state
   * @param input The strongly-typed input parameters for the task
   * @return ExecutionResult containing task outcome and optional output payload
   * @throws TaskExecutionException if task execution fails
   * @throws IllegalStateException if task is invoked in invalid state
   */
  ExecutionResult execute(ProcessInstance context, Object input);

  /**
   * Handles task completion notification, including both success and failure scenarios. This method
   * is invoked after task execution completes, whether synchronously or asynchronously.
   *
   * @param context The process instance providing execution context
   * @param signal The completion signal indicating success/failure
   * @param payload The execution result payload (may be null)
   */
  void handleCompletion(ProcessInstance context, CompletionSignal signal, Object payload);

  /**
   * Validates the task input parameters before execution. Default implementation performs basic
   * null checks.
   *
   * @param input The input parameters to validate
   * @throws jakarta.validation.ValidationException if validation fails
   */
  default void validateInput(Object input) {
    if (input == null) {
      throw new ValidationException("Task input cannot be null");
    }
  }

  /**
   * Performs cleanup operations when task execution is cancelled or terminated. Default
   * implementation is no-op but can be overridden for resource cleanup.
   *
   * @param context The process instance context
   */
  default void cleanup(ProcessInstance context) {
   // Default no-op implementation
  }
}
