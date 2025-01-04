/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.api.domain;

import java.util.List;
import java.util.Map;

/**
 * Defines a single unit of work within a process. A task represents an atomic operation that needs
 * to be performed as part of the larger process. Tasks are connected to other tasks through
 * transitions, forming the process flow.
 */
public interface TaskDefinition {

  /**
   * Returns the unique identifier of the task within its process. This ID is used to reference the
   * task in transitions and process execution.
   *
   * @return the task identifier
   */
  String getId();

  /**
   * Returns the input parameters required for this task's execution. The map contains parameter
   * names and their corresponding specifications. These inputs must be provided when the task is
   * executed.
   *
   * @return map of input parameter names to their specifications
   */
  Map<String, Object> getInputs();

  /**
   * Returns the list of possible transitions from this task to other tasks. These transitions
   * define the possible paths the process can take after this task completes. Multiple transitions
   * enable branching, conditional flows, and parallel execution paths.
   *
   * @return list of transitions to next tasks
   */
  List<Transition> getNextTransitions();
}
