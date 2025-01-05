/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.api.domain;

import com.upo.utilities.filter.api.Filter;

/**
 * Defines a connection between tasks in a process, representing possible paths of execution flow.
 * Transitions can be conditional, enabling dynamic process behavior based on task outputs or
 * external conditions.
 */
public interface Transition {

  /**
   * Returns the type of transition, indicating how the process should flow. Common types might
   * include "default", "conditional", "parallel", or "error". The type influences how the
   * orchestrator handles the transition during execution.
   *
   * @return the transition type identifier
   */
  String getType();

  /**
   * Returns the ID of the task that should be executed when this transition is taken. This creates
   * the connection between the source task and its possible next tasks.
   *
   * @return the ID of the destination task
   */
  String getNextTaskId();

  /**
   * Returns the predicate that determines whether this transition should be taken. The filter
   * evaluates runtime data to decide if the transition is valid. For unconditional transitions,
   * this might return null or a filter that always returns true.
   *
   * @return the filter predicate for this transition
   */
  Filter getPredicate();
}
