/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.api.domain;

/**
 * Represents a task's operation type within a process flow. Identifies whether a task is a simple
 * execution unit or a control flow operator.
 */
public enum TaskOperator {

  /** Standard task that performs a unit of work. */
  TASK,
  /**
   * Conditional flow control. Evaluates conditions and directs flow to appropriate path. Can be
   * used for: - if-else branching - switch-case selection - multi-way branching
   */
  CONDITIONAL,

  /** Loop start operator. Begins a section of repeatable tasks. */
  LOOP,

  /**
   * Early loop termination. Exits the current loop immediately. Can include optional break
   * condition in input schema.
   */
  BREAK,

  /**
   * Skips to next loop iteration. Skips remaining tasks in current iteration. Can include optional
   * continue condition in input schema.
   */
  CONTINUE,

  /** Parallel execution start. Splits flow into multiple concurrent paths. */
  FORK,

  /**
   * Synchronization point for parallel execution. Waits for all parallel paths to complete before
   * continuing.
   */
  JOIN,

  /**
   * Early process/scope termination. Can be used to: - Return from current process with result -
   * Exit from current scope with value - Early termination with output Output schema defines the
   * return value structure.
   */
  RETURN,
}
