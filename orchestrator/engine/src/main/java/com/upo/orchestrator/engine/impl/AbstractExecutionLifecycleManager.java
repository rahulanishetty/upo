/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import com.upo.orchestrator.engine.ProcessOutcome;
import com.upo.orchestrator.engine.ProcessServices;
import com.upo.orchestrator.engine.Signal;
import com.upo.orchestrator.engine.impl.events.LifecycleEvent;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.ExecutionLifecycleManager;
import com.upo.orchestrator.engine.services.ProcessInstanceStore;
import com.upo.orchestrator.engine.services.VariableStore;

public abstract class AbstractExecutionLifecycleManager implements ExecutionLifecycleManager {

  protected abstract void handleEvent(LifecycleEvent lifecycleEvent);

  @Override
  public void startProcess(String processDefinitionId, Object payload) {
    LifecycleEvent.StartProcess startProcess = new LifecycleEvent.StartProcess();
    startProcess.setProcessDefinitionId(processDefinitionId);
    startProcess.setPayload(payload);
    handleEvent(startProcess);
  }

  @Override
  public void startInstanceWithPayload(String instanceId, Object payload) {
    LifecycleEvent.StartProcessInstance startInstance = new LifecycleEvent.StartProcessInstance();
    startInstance.setInstanceId(instanceId);
    startInstance.setPayload(payload);
    handleEvent(startInstance);
  }

  @Override
  public void signalProcess(
      ProcessInstance processInstance, ProcessInstance targetInstance, Signal signal) {
    signalProcess(processInstance, targetInstance.getId(), signal);
  }

  @Override
  public void signalProcess(
      ProcessInstance processInstance, String targetInstanceId, Signal signal) {
    LifecycleEvent.SignalProcess signalProcess = new LifecycleEvent.SignalProcess();
    signalProcess.setProcessInstanceId(targetInstanceId);
    signalProcess.setSignal(signal);
    handleEvent(signalProcess);
  }

  @Override
  public void continueProcessFromTask(String processInstanceId, String taskId) {
    LifecycleEvent.ContinueProcessFromTask continueProcessFromTask =
        new LifecycleEvent.ContinueProcessFromTask();
    continueProcessFromTask.setProcessInstanceId(processInstanceId);
    continueProcessFromTask.setTaskId(taskId);
    handleEvent(continueProcessFromTask);
  }

  @Override
  public void notifyCompletion(ProcessInstance processInstance, ProcessOutcome processOutcome) {
   // TODO implement this.
  }

  @Override
  public void cleanup(ProcessInstance processInstance) {
    ProcessServices processServices = processInstance.getProcessEnv().getProcessServices();
    VariableStore variableStore = processServices.getService(VariableStore.class);
    variableStore.deleteProcessVariables(processInstance.getId());

    ProcessInstanceStore processInstanceStore =
        processServices.getService(ProcessInstanceStore.class);
    processInstanceStore.deleteById(processInstance.getId());
  }
}
