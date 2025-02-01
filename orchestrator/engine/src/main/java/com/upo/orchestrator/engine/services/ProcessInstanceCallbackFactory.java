/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.services;

import java.util.Map;

import com.upo.orchestrator.engine.ProcessInstanceCallback;
import com.upo.orchestrator.engine.models.ProcessInstance;

/** Factory for creating process-instance-specific post-commit actions. */
public interface ProcessInstanceCallbackFactory {

  void register(ProcessInstanceCallbackBuilder builder);

  /**
   * Creates callback handler for post-commit process operations.
   *
   * @param processInstance Current process instance context
   * @param callbackType Type of callback to create (e.g. START_SUBPROCESS)
   * @param callbackData Operation-specific data
   */
  ProcessInstanceCallback createCallback(
      ProcessInstance processInstance, String callbackType, Map<String, Object> callbackData);
}
