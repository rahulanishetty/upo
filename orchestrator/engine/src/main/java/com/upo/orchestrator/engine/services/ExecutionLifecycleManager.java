/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.services;

import com.upo.orchestrator.engine.ProcessOutcome;
import com.upo.orchestrator.engine.Signal;
import com.upo.orchestrator.engine.models.ProcessInstance;

/**
 * Manages the lifecycle of process executions including start, signal handling, and completion.
 * Provides operations for process control and state management across different execution phases.
 */
public interface ExecutionLifecycleManager {

  /**
   * Starts a new process execution with the given definition and payload.
   *
   * @param processDefinitionId Unique identifier of the process definition to execute
   * @param payload Initial data to populate process variables (can be null)
   * @throws IllegalArgumentException if processDefinitionId is null or empty
   */
  void startProcess(String processDefinitionId, Object payload);

  /**
   * Starts a new process instance with a specified ID and payload.
   *
   * @param instanceId Unique identifier for the new process instance
   * @param payload Initial data to populate process variables (can be null)
   */
  void startInstance(String instanceId, Object payload);

  /**
   * Signals a target process instance using the source instance context. Used for inter-process
   * communication and state transitions.
   *
   * @param sourceInstance The process instance sending the signal
   * @param targetInstance The process instance receiving the signal
   * @param signal The signal containing flow control information
   */
  void signalProcess(ProcessInstance sourceInstance, ProcessInstance targetInstance, Signal signal);

  /**
   * Signals a target process instance by ID using the source instance context.
   *
   * @param sourceInstance The process instance sending the signal
   * @param targetInstanceId ID of the process instance to receive the signal
   * @param signal The signal containing flow control information
   * @throws IllegalArgumentException if any parameter is null/empty
   */
  void signalProcess(ProcessInstance sourceInstance, String targetInstanceId, Signal signal);

  /**
   * Executes a process starting from the specified task. This method initiates process execution at
   * the given task ID.
   *
   * @param processInstanceId ID of the process instance to execute
   * @param taskId ID of the task to start execution from
   */
  void executeFromTask(String processInstanceId, String taskId);

  /**
   * Performs cleanup of process resources and state. Should be called after process completion or
   * termination.
   *
   * @param processInstance The process instance to clean up
   */
  void cleanupProcess(ProcessInstance processInstance);

  /**
   * Notifies completion handlers of process completion with outcome. Used to inform listeners about
   * final process state.
   *
   * @param processInstance The completed process instance
   * @param outcome The final outcome of the process execution
   */
  void notifyCompletion(ProcessInstance processInstance, ProcessOutcome outcome);
}
