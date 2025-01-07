/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.models;

import java.util.Map;

/**
 * Represents a signal that can be sent to a running task. Signals are used to communicate with
 * tasks that are waiting for external events or running asynchronously. They allow external systems
 * or users to provide data or control task execution.
 */
public class Signal {

  /**
   * The type of signal being sent. Signal types are defined by tasks based on their execution
   * pattern and supported interactions. Common signal types include: - RESUME: Task completion with
   * result - CANCEL: Request to cancel execution
   */
  private String type;

  /**
   * Additional data associated with the signal. The structure and content of details depends on the
   * signal type and task requirements.
   */
  private Map<String, Object> details;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Map<String, Object> getDetails() {
    return details;
  }

  public void setDetails(Map<String, Object> details) {
    this.details = details;
  }
}
