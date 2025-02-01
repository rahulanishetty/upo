/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.upo.orchestrator.engine.models.ProcessInstance;

/**
 * Runtime representation of a task that handles its execution lifecycle. This interface provides
 * methods for executing tasks and handling signals during task execution. Each task type (HTTP,
 * Java, Script, etc.) will have its own implementation based on its execution requirements.
 */
public interface TaskRuntime {

  /**
   * @return the taskId corresponding to this runtime
   */
  String getTaskId();

  /**
   * Executes this task within the given process instance. Task execution can: - Complete
   * synchronously - Start asynchronous operation - Initiate external interaction based on the
   * task's execution pattern.
   *
   * @param processInstance instance containing execution state and context
   * @return Next containing IDs of tasks to execute next, empty if process should end
   */
  Next execute(ProcessInstance processInstance);

  /**
   * Handles a signal received during task execution. Signals are used to: - Complete asynchronous
   * operations - Handle external interactions - Control task execution (cancel, retry)
   *
   * @param processInstance instance containing execution state
   * @param status status of the task
   * @param payload additional data associated with this signal processing
   * @return Next containing IDs of tasks to execute next, empty if no further tasks
   * @throws IllegalArgumentException if signal type not supported
   * @throws IllegalStateException if signal not valid in current state
   */
  Next handleSignal(
      ProcessInstance processInstance, TaskResult.Status status, Map<String, Object> payload);

  /**
   * Represents the next task(s) to be executed in the process flow. Used to determine process
   * navigation after task execution or signal handling.
   */
  interface Next {
    /**
     * Empty next tasks indicator. Used when: - Process reaches end - Task is waiting for signal -
     * No further tasks to execute
     */
    Next EMPTY = Collections::emptyList;

    /**
     * Returns list of task IDs to execute next.
     *
     * @return list of next task IDs, empty if no transition
     */
    List<Transition> transitions();
  }
}
