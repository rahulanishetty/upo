/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import java.util.List;

import com.upo.orchestrator.engine.models.ProcessInstance;

/**
 * Resolves the next possible transitions for a task during process execution. This interface
 * enables dynamic resolution of process flow rather than relying on statically defined transitions,
 * allowing for: - Runtime decision making - External service-based routing - Dynamic workflow
 * modifications - Context-aware transition selection
 *
 * @see Transition
 * @see TaskRuntime
 * @see ProcessInstance
 */
public interface TransitionResolver {

  /**
   * Resolves the list of possible transitions for the current task execution.
   *
   * @param taskRuntime The current task runtime
   * @param instance The current process instance
   * @param result The execution result of the current task
   * @return List of valid transitions for the next step
   */
  List<Transition> resolveTransitions(
      TaskRuntime taskRuntime, ProcessInstance instance, TaskResult result);
}
