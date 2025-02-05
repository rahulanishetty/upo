/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.callbacks;

import java.util.Map;

import com.upo.orchestrator.engine.ProcessInstanceCallback;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.ExecutionLifecycleManager;
import com.upo.orchestrator.engine.services.ProcessInstanceCallbackBuilder;
import com.upo.orchestrator.engine.services.ProcessInstanceCallbackFactory;
import com.upo.utilities.ds.CollectionUtils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class StartSubProcessProcessInstanceCallbackBuilder
    implements ProcessInstanceCallbackBuilder {

  public static final String TYPE = "START_SUB_PROCESS";
  public static final String INSTANCE_ID = "processId";
  public static final String PAYLOAD = "payload";

  @Inject
  public StartSubProcessProcessInstanceCallbackBuilder(
      ProcessInstanceCallbackFactory processInstanceCallbackFactory) {
    processInstanceCallbackFactory.register(this);
  }

  @Override
  public ProcessInstanceCallback build(
      ProcessInstance processInstance, Map<String, Object> callbackData) {
    return () -> {
      String instanceId = CollectionUtils.getStringValue(callbackData, INSTANCE_ID);
      Object payload = CollectionUtils.getValue(callbackData, PAYLOAD);
      getExecutionLifecycleManager(processInstance).startInstance(instanceId, payload);
    };
  }

  @Override
  public String getType() {
    return TYPE;
  }

  private ExecutionLifecycleManager getExecutionLifecycleManager(ProcessInstance processInstance) {
    return processInstance
        .getProcessEnv()
        .getProcessServices()
        .getService(ExecutionLifecycleManager.class);
  }
}
