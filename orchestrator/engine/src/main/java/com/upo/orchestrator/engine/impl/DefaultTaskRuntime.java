/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.models.CompletionSignal;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.models.ProcessVariable;
import com.upo.orchestrator.engine.services.*;
import com.upo.utilities.ds.CollectionUtils;
import com.upo.utilities.ds.Pair;

public abstract class DefaultTaskRuntime implements TaskRuntime {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTaskRuntime.class);

  private String taskId;
  private ResolvableValue inputs;
  private Set<Pair<String, Variable.Type>> dependencies;

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public void setInputs(Object inputs) {
    this.inputs = DefaultInputValueResolver.getInstance().resolve(inputs);
    if (this.inputs == null) {
      this.dependencies = null;
    } else {
      this.dependencies = this.inputs.getVariableDependencies();
    }
  }

  @Override
  public Next execute(ProcessInstance processInstance) {
    Optional<Next> next = beforeTaskExecution(processInstance);
    if (next.isPresent()) {
     // short-circuited
      return next.get();
    }
    ExecutionResult executionResult = _execute(processInstance);
    return afterTaskExecution(processInstance, executionResult)
        .orElseGet(() -> onTaskCompletion(processInstance, executionResult));
  }

  @Override
  public Next handleSignal(
      ProcessInstance processInstance, CompletionSignal signal, Map<String, Object> payload) {
    return onTaskCompletion(processInstance, ExecutionResult.continueWithNoVariables());
  }

  private ExecutionResult _execute(ProcessInstance processInstance) {
    VariableUtils.loadMissingReferencedVariables(processInstance, dependencies);
    ExecutionResult executionResult = null;
    try {
      executionResult = doExecute(processInstance);
    } catch (Throwable th) {
      executionResult = toFailure(processInstance, th);
    }
    return executionResult;
  }

  protected abstract ExecutionResult doExecute(ProcessInstance processInstance);

  protected void executeAfterSave(
      ProcessInstance processInstance, Map<String, Object> afterSaveCommand) {
    throw new UnsupportedOperationException("Override this method to support afterSaveCommand");
  }

  protected ExecutionResult toFailure(ProcessInstance processInstance, Throwable throwable) {
    throw new UnsupportedOperationException("TODO Implement this!");
  }

  private Optional<Next> beforeTaskExecution(ProcessInstance processInstance) {
    ProcessServices processServices = getServices(processInstance);
    EnvironmentProvider environmentProvider = processServices.getEnvironmentProvider();
    if (environmentProvider.isShutdownInProgress()) {
     // check if shutdown is in progress, if yes throw an event to resume from this task
      processInstance.setStatus(ExecutionResult.Status.WAIT);
      if (saveProcessInstance(processInstance, ExecutionResult.Status.CONTINUE)) {
        ExecutionLifecycleManager executionLifecycleManager =
            processServices.getExecutionLifecycleManager();
        executionLifecycleManager.resumeProcessFromTask(processInstance, this);
      }
      return Optional.of(Next.EMPTY);
    }
    if (Objects.equals(taskId, processInstance.getTerminateAtTaskId())) {
      handleInstanceCompletion(processInstance, "TERMINATED_AT_TASK_ID");
      return Optional.of(Next.EMPTY);
    }
    processInstance.setPrevTaskId(processInstance.getCurrTaskId());
    processInstance.setCurrTaskId(taskId);
    processInstance.setCurrentTaskStartTime(System.currentTimeMillis());
    if (processInstance.incrementTaskCount() % 100 == 0) {
     // periodically flush accumulated variables and state to store
      if (!saveProcessInstance(processInstance, ExecutionResult.Status.CONTINUE)) {
        flushNewVariablesIfAny(processInstance);
        return Optional.of(Next.EMPTY);
      }
    }
    ExecutionLifecycleAuditor lifecycleAuditor = processServices.getLifecycleAuditor();
    lifecycleAuditor.beforeExecution(this, processInstance);
    return Optional.empty();
  }

  private void handleInstanceCompletion(ProcessInstance processInstance, String code) {}

  private Optional<Next> afterTaskExecution(
      ProcessInstance processInstance, ExecutionResult executionResult) {
    processInstance.setCurrentTaskEndTime(System.currentTimeMillis());
    if (executionResult.getStatus() != ExecutionResult.Status.CONTINUE) {
      processInstance.setStatus(executionResult.getStatus());
      if (!saveProcessInstance(processInstance, ExecutionResult.Status.CONTINUE)) {
        flushNewVariablesIfAny(processInstance);
        LOGGER.error("somethings wrong, execution instance not in expected state");
        return Optional.of(Next.EMPTY);
      } else {
        Map<String, Object> afterSaveCommand = executionResult.getAfterSaveCommand();
        if (afterSaveCommand != null) {
          executeAfterSave(processInstance, afterSaveCommand);
        }
      }
    }
    ExecutionLifecycleAuditor lifecycleAuditor = getServices(processInstance).getLifecycleAuditor();
    lifecycleAuditor.afterExecution(this, processInstance);
    return Optional.empty();
  }

  private Next onTaskCompletion(ProcessInstance processInstance, ExecutionResult executionResult) {
    processInstance.setCurrentTaskEndTime(System.currentTimeMillis());
    return Next.EMPTY;
  }

  protected void flushNewVariablesIfAny(ProcessInstance processInstance) {
    VariableContainer variableContainer = processInstance.getVariableContainer();
    List<ProcessVariable> newVariables = variableContainer.getNewVariables();
    if (CollectionUtils.isEmpty(newVariables)) {
      return;
    }
    ProcessServices processServices = getServices(processInstance);
    VariableStore variableStore = processServices.getVariableStore();
    for (ProcessVariable newVariable : newVariables) {
      newVariable.initId(processInstance);
    }
    variableStore.saveMany(newVariables);
    variableContainer.clearNewVariables();
  }

  protected boolean saveProcessInstance(
      ProcessInstance processInstance, ExecutionResult.Status expectedStatus) {
    ProcessServices processServices = getServices(processInstance);
    ProcessInstanceStore instanceStore = processServices.getInstanceStore();
    if (instanceStore.save(processInstance, expectedStatus)) {
      processInstance.setTaskCountSinceLastFlush(0L);
      flushNewVariablesIfAny(processInstance);
      return true;
    }
    return false;
  }

  protected ProcessServices getServices(ProcessInstance processInstance) {
    return processInstance.getProcessEnv().getProcessServices();
  }

  private Variable skippedInput() {
    ProcessVariable variable = new ProcessVariable();
    variable.setTaskId(taskId);
    variable.setType(Variable.Type.INPUT);
    variable.setPayload(Map.of("skipped", "true"));
    return variable;
  }
}
