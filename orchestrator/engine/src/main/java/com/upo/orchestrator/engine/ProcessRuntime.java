/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import com.upo.orchestrator.api.domain.ProcessDefinition;

/**
 * Runtime representation of a process that handles the execution lifecycle. Manages process
 * initialization, execution flow, and signal handling. Each process instance maintains its own
 * state and execution context.
 */
public interface ProcessRuntime {

  /** Returns the process definition */
  ProcessDefinition getDefinition();

  /** Returns shared task runtime */
  TaskRuntime getOrCreateTaskRuntime(String taskId);

  /** Creates an executor for specific execution strategy */
  ProcessExecutor createExecutor(ExecutionStrategy strategy);
}
