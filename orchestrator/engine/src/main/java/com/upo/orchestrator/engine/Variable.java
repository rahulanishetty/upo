/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

/**
 * Represents a variable created during process execution. Variables capture task inputs, outputs,
 * errors, and state changes as tasks execute within a process.
 */
public interface Variable {

  /**
   * Returns the ID of the task that created this variable. Used to track which task produced or
   * consumed this variable during process execution.
   *
   * @return task identifier
   */
  String getTaskId();

  /**
   * Returns the type of this variable. Variable types indicate the variable's role in process
   * execution: - INPUT: Processed input values used by task - OUTPUT: Values produced by task
   * execution - ERROR: Error details when task fails - STATE: State changes during task execution
   *
   * @return variable type
   */
  Variables.Type getType();

  /**
   * Returns the actual data/value of this variable. The structure and content of payload depends on
   * the variable type and task implementation.
   *
   * @return variable payload
   */
  Object getPayload();
}
