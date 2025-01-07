/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.Map;

import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.models.Signal;
import com.upo.utilities.filter.impl.FilterEvaluator;

public abstract class DefaultTaskRuntime implements TaskRuntime {

  private String taskId;
  private FilterEvaluator<ProcessInstance> evaluator;

  @Override
  public Next execute(ProcessInstance processInstance) {
    beforeTaskExecution(processInstance);
    ExecutionResult executionResult = null;
    try {
      if (evaluator.evaluate(processInstance)) {
        executionResult = doExecute(processInstance);
      } else {
        executionResult = ExecutionResult.CONTINUE;
      }
    } catch (Throwable th) {
      executionResult = toFailure(processInstance, th);
    }
    afterTaskExecution(processInstance, executionResult);
    return onTaskCompletion(processInstance, executionResult);
  }

  @Override
  public Next handleSignal(
      ProcessInstance processInstance, Signal signal, Map<String, Object> payload) {
    return onTaskCompletion(processInstance, ExecutionResult.CONTINUE);
  }

  protected abstract ExecutionResult doExecute(ProcessInstance processInstance);

  protected abstract void executeAfterSave(
      ProcessInstance processInstance, Map<String, Object> afterSaveCommand);

  protected ExecutionResult toFailure(ProcessInstance processInstance, Throwable throwable) {
    throw new UnsupportedOperationException("TODO Implement this!");
  }

  private void beforeTaskExecution(ProcessInstance processInstance) {
    processInstance.setPrevTaskId(processInstance.getCurrTaskId());
    processInstance.setCurrTaskId(taskId);
    processInstance.setCurrentTaskStartTime(System.currentTimeMillis());
  }

  private void afterTaskExecution(
      ProcessInstance processInstance, ExecutionResult executionResult) {
    processInstance.setCurrentTaskEndTime(System.currentTimeMillis());
    if (executionResult.getStatus() == ExecutionResult.Status.WAIT) {}
  }

  private Next onTaskCompletion(ProcessInstance processInstance, ExecutionResult executionResult) {
    processInstance.setCurrentTaskEndTime(System.currentTimeMillis());
    return Next.EMPTY;
  }
}
