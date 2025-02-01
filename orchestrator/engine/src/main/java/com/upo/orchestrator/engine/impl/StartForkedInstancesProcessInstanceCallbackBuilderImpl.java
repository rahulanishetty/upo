/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.upo.orchestrator.engine.ProcessInstanceCallback;
import com.upo.orchestrator.engine.ProcessServices;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.ExecutionLifecycleManager;
import com.upo.orchestrator.engine.services.ProcessInstanceCallbackBuilder;
import com.upo.orchestrator.engine.services.ProcessInstanceCallbackFactory;
import com.upo.orchestrator.engine.services.ProcessInstanceStore;
import com.upo.utilities.ds.CollectionUtils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class StartForkedInstancesProcessInstanceCallbackBuilderImpl
    implements ProcessInstanceCallbackBuilder {

  public static final String TYPE = "START_FORKED_INSTANCES";
  public static final String TASK_ID = "taskId";
  public static final String INSTANCE_ID = "instanceId";
  public static final String INSTANCES = "instances";
  public static final String WAIT_ON_INSTANCE_IDS = "waitOnInstanceIds";

  @Inject
  public StartForkedInstancesProcessInstanceCallbackBuilderImpl(
      ProcessInstanceCallbackFactory callbackFactory) {
    callbackFactory.register(this);
  }

  @Override
  public ProcessInstanceCallback build(
      ProcessInstance processInstance, Map<String, Object> callbackData) {
    return () -> {
     //noinspection unchecked
      Collection<String> waitOnInstanceIds =
          (Collection<String>) CollectionUtils.getValue(callbackData, WAIT_ON_INSTANCE_IDS);
      if (CollectionUtils.isNotEmpty(waitOnInstanceIds)) {
        getProcessInstanceStore(processInstance)
            .addWaitingOnInstanceIds(processInstance, waitOnInstanceIds);
      }

     //noinspection unchecked
      List<Map<String, Object>> instances =
          (List<Map<String, Object>>) CollectionUtils.getValue(callbackData, INSTANCES);
      if (CollectionUtils.isNotEmpty(instances)) {
        ExecutionLifecycleManager executionLifecycleManager =
            getExecutionLifecycleManager(processInstance);
        for (Map<String, Object> instance : instances) {
          String instanceId = CollectionUtils.getStringValue(instance, INSTANCE_ID);
          String taskId = CollectionUtils.getStringValue(instance, TASK_ID);
          executionLifecycleManager.continueProcessFromTask(instanceId, taskId);
        }
      }
    };
  }

  @Override
  public String getType() {
    return TYPE;
  }

  private ExecutionLifecycleManager getExecutionLifecycleManager(ProcessInstance processInstance) {
    ProcessServices processServices = processInstance.getProcessEnv().getProcessServices();
    return processServices.getService(ExecutionLifecycleManager.class);
  }

  private ProcessInstanceStore getProcessInstanceStore(ProcessInstance processInstance) {
    ProcessServices processServices = processInstance.getProcessEnv().getProcessServices();
    return processServices.getService(ProcessInstanceStore.class);
  }
}
