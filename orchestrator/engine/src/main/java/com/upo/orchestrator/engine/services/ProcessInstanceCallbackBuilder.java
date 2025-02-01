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

/**
 * Builder interface for constructing and handling process callbacks. Implementations of this
 * interface are responsible for building callback responses and updating the process instance state
 * based on callback data.
 */
public interface ProcessInstanceCallbackBuilder {

  ProcessInstanceCallback build(ProcessInstance processInstance, Map<String, Object> callbackData);

  String getType();
}
