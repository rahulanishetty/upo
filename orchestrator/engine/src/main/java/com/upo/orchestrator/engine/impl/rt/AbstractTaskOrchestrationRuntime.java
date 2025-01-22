/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.rt;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.impl.VariableUtils;
import com.upo.orchestrator.engine.models.CompletionSignal;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.models.ProcessVariable;
import com.upo.orchestrator.engine.services.*;
import com.upo.utilities.ds.CollectionUtils;

/**
 * Abstract base class that implements core task orchestration and execution flow while leaving
 * task-specific execution details to concrete implementations.
 */
public abstract class AbstractTaskOrchestrationRuntime extends AbstractTaskRuntime
    implements TaskRuntime {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractTaskOrchestrationRuntime.class);

  public AbstractTaskOrchestrationRuntime(ProcessRuntime parent, String taskId) {
    super(parent, taskId);
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
      if (skipCondition == null || !skipCondition.evaluate(processInstance)) {
        executionResult = doExecute(processInstance);
      } else {
        executionResult = ExecutionResult.continueWithVariables(List.of(skippedInput()));
      }
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
    EnvironmentProvider environmentProvider = processServices.getService(EnvironmentProvider.class);
    if (environmentProvider.isShutdownInProgress()) {
     // check if shutdown is in progress, if yes throw an event to resume from this task
      processInstance.setStatus(ExecutionResult.Status.WAIT);
      if (saveProcessInstance(processInstance, ExecutionResult.Status.CONTINUE)) {
        ExecutionLifecycleManager executionLifecycleManager =
            processServices.getService(ExecutionLifecycleManager.class);
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
    ExecutionLifecycleAuditor lifecycleAuditor =
        processServices.getService(ExecutionLifecycleAuditor.class);
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
    ExecutionLifecycleAuditor lifecycleAuditor =
        getServices(processInstance).getService(ExecutionLifecycleAuditor.class);
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
    VariableStore variableStore = processServices.getService(VariableStore.class);
    for (ProcessVariable newVariable : newVariables) {
      newVariable.initId(processInstance);
    }
    variableStore.saveMany(newVariables);
    variableContainer.clearNewVariables();
  }

  protected boolean saveProcessInstance(
      ProcessInstance processInstance, ExecutionResult.Status expectedStatus) {
    ProcessServices processServices = getServices(processInstance);
    ProcessInstanceStore instanceStore = processServices.getService(ProcessInstanceStore.class);
    if (instanceStore.save(processInstance, expectedStatus)) {
      processInstance.setTaskCountSinceLastFlush(0L);
      flushNewVariablesIfAny(processInstance);
      return true;
    }
    return false;
  }

  private Variable skippedInput() {
    ProcessVariable variable = new ProcessVariable();
    variable.setTaskId(taskId);
    variable.setType(Variable.Type.INPUT);
    variable.setPayload(Map.of("skipped", "true"));
    return variable;
  }
}
