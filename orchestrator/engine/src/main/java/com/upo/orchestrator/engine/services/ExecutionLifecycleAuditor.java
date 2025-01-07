/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.services;

import java.util.Collection;

import com.upo.orchestrator.engine.TaskRuntime;
import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.models.ProcessInstance;

/**
 * Audits the execution lifecycle of tasks within a process. Captures execution events, state
 * changes, and variables at different points in the task execution lifecycle for auditing and
 * monitoring.
 */
public interface ExecutionLifecycleAuditor {
  /**
   * Called before task execution begins. Captures the initial state and context before task
   * execution. Useful for: - Recording task start time - Logging execution context - Recording task
   * metadata
   *
   * @param taskRuntime task about to execute
   * @param processInstance process instance context
   */
  void beforeExecution(TaskRuntime taskRuntime, ProcessInstance processInstance);

  /**
   * Called immediately after task execution. Captures the immediate execution results and state
   * changes. Useful for: - Capturing execution time - Logging execution status - Recording state
   * changes
   *
   * @param taskRuntime executed task
   * @param processInstance process instance context
   */
  void afterExecution(TaskRuntime taskRuntime, ProcessInstance processInstance);

  /**
   * Called when task completes its lifecycle. For async/external tasks, this is called after final
   * completion, which might be different from afterExecution. Useful for: - Recording final task
   * state - Capturing total task duration - Logging completion status
   *
   * @param taskRuntime completed task
   * @param processInstance process instance context
   */
  void onCompletion(TaskRuntime taskRuntime, ProcessInstance processInstance);

  /**
   * Records variables produced during task execution. Captures all variables (input, output, error,
   * state) that were created or modified during task execution.
   *
   * @param variables collection of variables to record
   * @param taskRuntime task that produced the variables
   * @param processInstance process instance context
   */
  void recordVariables(
      Collection<Variable> variables, TaskRuntime taskRuntime, ProcessInstance processInstance);
}
