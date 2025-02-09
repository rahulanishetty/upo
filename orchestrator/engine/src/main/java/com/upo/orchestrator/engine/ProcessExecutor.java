/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import java.util.Map;

import com.upo.orchestrator.engine.services.ProcessServices;

/** Handles actual process execution using specified strategy */
public interface ProcessExecutor {
  /** Returns the execution services for this executor */
  ProcessServices getServices();

  /** Returns the process runtime this executor belongs to */
  ProcessRuntime getRuntime();

  /**
   * Starts a new process execution with the provided initial payload. This will: - Create a new
   * process instance - Initialize process state with payload - Begin execution from the start task
   *
   * <p>Process execution continues until: - Process completes - A task transitions to WAIT status -
   * An error occurs
   *
   * @param payload initial data to populate process variables
   * @return process instance identifier that can be used to send signals
   * @throws IllegalArgumentException if payload validation fails
   */
  String start(Map<String, Object> payload);

  /**
   * Starts a new process execution with a specified instance ID and initial payload. This method: -
   * Initializes process state with payload - Begins execution from the start task
   *
   * <p>Process execution continues until: - Process completes - A task transitions to WAIT status -
   * An error occurs
   *
   * @param instanceId Unique identifier for the new process instance
   * @param payload Initial data to populate process variables. Can be null if no initial data is
   *     needed.
   * @throws IllegalArgumentException if instanceId is null/empty or payload validation fails
   */
  void start(String instanceId, Map<String, Object> payload);

  /**
   * Handles a signal received during process execution. Signals are forwarded to the appropriate
   * task based on current process state. Signal handling can: - Resume waiting tasks - Update
   * process state
   *
   * @param processInstanceId identifier of the target process instance
   * @param signal signal information
   * @throws IllegalArgumentException if process instance not found or signal/payload is invalid
   * @throws IllegalStateException if process instance is not in a state to handle signals
   */
  void signal(String processInstanceId, Signal signal);
}
