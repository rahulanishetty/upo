/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import com.upo.orchestrator.engine.services.*;

/**
 * Provides core services required for process execution. Acts as a central point of access for
 * process management, variable handling, and execution auditing services.
 */
public interface ProcessServices {

  /**
   * Returns the process instance store service. This service is responsible for: - Creating process
   * instances - Loading/saving process instance state - Managing instance lifecycle - Handling
   * parent-child relationships - Instance state persistence
   *
   * @return process instance store service
   */
  ProcessInstanceStore getInstanceStore();

  /**
   * Returns the variable store service. This service is responsible for: - Managing process
   * variables - Variable state persistence - Variable resolution - Variable scoping - Variable
   * cleanup
   *
   * @return variable manager service
   */
  VariableStore getVariableStore();

  /**
   * Returns the process manager service. This service is responsible for: - Managing process
   * runtimes
   *
   * @return process manager service
   */
  ProcessManager getProcessManager();

  /**
   * Returns the execution lifecycle manager service. This service is responsible for: - spawning
   * process instances - resuming process instances - signalling process instances
   *
   * @return process manager service
   */
  ExecutionLifecycleManager getExecutionLifecycleManager();

  /**
   * Returns the execution lifecycle auditor service. This service is responsible for: - Recording
   * execution events - Capturing task inputs/outputs - Tracking state changes - Logging execution
   * history - Maintaining audit trail
   *
   * @return execution lifecycle auditor service
   */
  ExecutionLifecycleAuditor getLifecycleAuditor();
}
