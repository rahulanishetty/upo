/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.upo.orchestrator.engine.ProcessInstanceCallback;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.ProcessInstanceCallbackBuilder;
import com.upo.orchestrator.engine.services.ProcessInstanceCallbackFactory;

import jakarta.inject.Singleton;

@Singleton
public class ProcessInstanceCallbackFactoryImpl implements ProcessInstanceCallbackFactory {

  private final Map<String, ProcessInstanceCallbackBuilder> callbackRegistry;

  public ProcessInstanceCallbackFactoryImpl() {
    this.callbackRegistry = new ConcurrentHashMap<>();
  }

  @Override
  public void register(ProcessInstanceCallbackBuilder builder) {
    ProcessInstanceCallbackBuilder existing =
        this.callbackRegistry.putIfAbsent(builder.getType(), builder);
    if (existing != null) {
      throw new IllegalStateException("duplicate callback for type: " + builder.getType());
    }
  }

  @Override
  public ProcessInstanceCallback createCallback(
      ProcessInstance processInstance, String callbackType, Map<String, Object> callbackData) {
    return null;
  }
}
