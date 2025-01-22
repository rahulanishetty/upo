/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import java.util.Map;
import java.util.Optional;

import com.upo.orchestrator.api.domain.ProcessDefinition;
import com.upo.utilities.filter.impl.FilterEvaluator;

/**
 * Runtime representation of a process that handles the execution lifecycle. Manages process
 * initialization, execution flow, and signal handling. Each process instance maintains its own
 * state and execution context.
 */
public interface ProcessRuntime {

  /** Returns the process details */
  ProcessDetails getDetails();

  /** Returns the process definition */
  ProcessDefinition getDefinition();

  /**
   * Retrieves an optional predicate filter used to determine whether the process should be
   * triggered based on the current context variables.
   *
   * <p>The predicate serves as a gate-keeping mechanism for process execution, allowing conditional
   * process initiation based on complex evaluation rules.
   *
   * <p>Key characteristics: - Provides a flexible, dynamic condition for process triggering -
   * Evaluates against a map of context variables - Allows fine-grained control over process
   * initiation
   *
   * <p>Use cases: - Implement complex activation conditions - Filter processes based on runtime
   * context - Provide conditional process execution
   *
   * <p>Example scenarios: - Skip process if certain conditions are not met - Validate prerequisite
   * conditions before process start - Implement business rule-based process filtering
   *
   * @return An optional {@link FilterEvaluator} that takes context variables and returns a boolean
   *     indicating whether the process should proceed
   * @see FilterEvaluator
   * @see ProcessDefinition
   */
  Optional<FilterEvaluator<Map<String, Object>>> getPredicate();

  /** Returns shared task runtime */
  TaskRuntime getOrCreateTaskRuntime(String taskId);

  /** Creates an executor for specific execution strategy */
  ProcessExecutor createExecutor(ExecutionStrategy strategy);

  /** Returns the core runtime services */
  ProcessServices getCoreServices();
}
