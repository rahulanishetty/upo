/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.Objects;

import com.upo.orchestrator.engine.ProcessOutcome;
import com.upo.orchestrator.engine.ProcessOutcomeSink;
import com.upo.orchestrator.engine.ProcessServices;
import com.upo.orchestrator.engine.Signal;
import com.upo.orchestrator.engine.impl.events.LifecycleEvent;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.ExecutionLifecycleManager;
import com.upo.orchestrator.engine.services.ProcessInstanceStore;
import com.upo.orchestrator.engine.services.VariableStore;

/**
 * Abstract implementation of ExecutionLifecycleManager that converts lifecycle operations into
 * events for processing. Subclasses need to implement the event handling logic.
 */
public abstract class AbstractExecutionLifecycleManager implements ExecutionLifecycleManager {

  /**
   * Handles lifecycle events. Subclasses must implement this to process different event types.
   *
   * @param lifecycleEvent The event to be handled
   * @throws IllegalArgumentException if event is null
   */
  protected abstract void handleEvent(LifecycleEvent lifecycleEvent);

  @Override
  public void startProcess(String processDefinitionId, Object payload) {
    Objects.requireNonNull(processDefinitionId, "Process definition ID cannot be null");

    LifecycleEvent.StartProcess startProcess = new LifecycleEvent.StartProcess();
    startProcess.setProcessDefinitionId(processDefinitionId);
    startProcess.setPayload(payload);
    handleEvent(startProcess);
  }

  @Override
  public void startInstance(String instanceId, Object payload) {
    Objects.requireNonNull(instanceId, "Instance ID cannot be null");

    LifecycleEvent.StartExistingInstance startInstance = new LifecycleEvent.StartExistingInstance();
    startInstance.setInstanceId(instanceId);
    startInstance.setPayload(payload);
    handleEvent(startInstance);
  }

  @Override
  public void signalProcess(
      ProcessInstance processInstance, ProcessInstance targetInstance, Signal signal) {
    Objects.requireNonNull(targetInstance, "Target instance cannot be null");
    signalProcess(processInstance, targetInstance.getId(), signal);
  }

  @Override
  public void signalProcess(
      ProcessInstance processInstance, String targetInstanceId, Signal signal) {
    Objects.requireNonNull(targetInstanceId, "Target instance ID cannot be null");
    Objects.requireNonNull(signal, "Signal cannot be null");

    LifecycleEvent.SignalProcess signalProcess = new LifecycleEvent.SignalProcess();
    signalProcess.setProcessInstanceId(targetInstanceId);
    signalProcess.setSignal(signal);
    handleEvent(signalProcess);
  }

  @Override
  public void executeFromTask(String processInstanceId, String taskId) {
    Objects.requireNonNull(processInstanceId, "Process instance ID cannot be null");
    Objects.requireNonNull(taskId, "Task ID cannot be null");

    LifecycleEvent.ExecuteFromTask executeFromTask = new LifecycleEvent.ExecuteFromTask();
    executeFromTask.setProcessInstanceId(processInstanceId);
    executeFromTask.setTaskId(taskId);
    handleEvent(executeFromTask);
  }

  @Override
  public void notifyCompletion(ProcessInstance processInstance, ProcessOutcome processOutcome) {
    ProcessOutcomeSink sink = processInstance.getSink();
    if (sink == null) {
      return;
    }
    sink.onOutcome(processInstance, processOutcome);
  }

  @Override
  public void cleanupProcess(ProcessInstance processInstance) {
    Objects.requireNonNull(processInstance, "Process instance cannot be null");

    ProcessServices processServices = processInstance.getProcessEnv().getProcessServices();
    VariableStore variableStore = processServices.getService(VariableStore.class);
    variableStore.deleteProcessVariables(processInstance.getId());

    ProcessInstanceStore processInstanceStore =
        processServices.getService(ProcessInstanceStore.class);
    processInstanceStore.deleteById(processInstance.getId());
  }
}
