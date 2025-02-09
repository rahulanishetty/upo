/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.services;

import com.upo.orchestrator.engine.impl.events.LifecycleEvent;

/** Handles lifecycle events for process execution control. */
public interface LifecycleEventHandler {

  /**
   * Processes a lifecycle event to control process execution.
   *
   * @param lifecycleEvent The event to handle, which can be: - StartProcess: Starts new process
   *     with definition - StartExistingInstance: Starts pre-created instance - ExecuteFromTask:
   *     Executes from specific task - SignalProcess: Handles process signals
   */
  void handle(LifecycleEvent lifecycleEvent);
}
