/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

/** Represents the process flow status after evaluating task result and transitions. */
public enum ProcessFlowStatus {
  /** Process should continue with the resolved transitions */
  CONTINUE,

  /** Process should wait for external signal/event */
  WAIT,

  /** Process execution completed successfully */
  COMPLETED,

  /** Process failed with no matching error handlers */
  FAILED,

  /** Process should be suspended */
  SUSPENDED
}
