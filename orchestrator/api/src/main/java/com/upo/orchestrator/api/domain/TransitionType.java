/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.api.domain;

/**
 * Defines the type of transition between tasks in a process. The type determines when and how the
 * transition is taken during process execution.
 */
public enum TransitionType {

  /**
   * Default transition path taken after successful task execution. Only one DEFAULT transition is
   * allowed per task. This transition is taken when: - Task completes successfully - No CONDITIONAL
   * transitions match
   */
  DEFAULT,

  /**
   * Conditional transition based on task output or process state. Multiple CONDITIONAL transitions
   * are allowed and evaluated in order. First transition whose predicate evaluates to true is
   * taken. If no predicates match, the DEFAULT transition is taken.
   */
  CONDITIONAL,

  /**
   * Error handling transition taken when task execution fails. Multiple ERROR transitions are
   * allowed and evaluated in order when an error occurs. Each ERROR transition can: - Handle
   * specific error types through its predicate - Route to error handling logic (by transitioning to
   * error handler task) - Skip the error (by transitioning to same task as DEFAULT)
   *
   * <p>If no ERROR transitions match or none are defined, the process fails.
   *
   * <p>Example predicates: - error.code == 'VALIDATION_ERROR' - error.retryable == true -
   * error.type == 'TimeoutException'
   */
  ERROR
}
