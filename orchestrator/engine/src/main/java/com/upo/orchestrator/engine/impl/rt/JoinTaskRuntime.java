/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.rt;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.ExecutionLifecycleManager;
import com.upo.orchestrator.engine.services.ProcessInstanceStore;
import com.upo.orchestrator.engine.services.VariableStore;
import com.upo.utilities.ds.CollectionUtils;

public class JoinTaskRuntime extends AbstractTaskOrchestrationRuntime {

  public JoinTaskRuntime(ProcessRuntime parent, String taskId) {
    super(parent, taskId);
  }

  public void join(
      ProcessInstance concurrentInstance,
      ProcessInstance parentInstance,
      ProcessFlowStatus flowStatus,
      TaskResult taskResult) {
    ProcessInstanceStore processInstanceStore =
        getService(concurrentInstance, ProcessInstanceStore.class);
    if (!processInstanceStore.removeCompletedInstanceId(
        parentInstance, concurrentInstance.getId())) {
      return;
    }
   // Copy variables from completed instance to parent
    copyVariablesFromConcurrentInstance(concurrentInstance, parentInstance);
   // there is a race condition here, where to multiple threads
   // could see remainingChildren as empty, and cause duplicate signals
   // however it wouldn't be an issue and is automatically handled by
   // handleSignal
    Set<String> remainingChildren = processInstanceStore.getRemainingChildren(parentInstance);

   // Signal parent if:
   // 1. No remaining children, OR
   // 2. Non-successful completion with remaining children (need to suspend them)
   // 3. Branch is returning a result.
    if (CollectionUtils.isEmpty(remainingChildren)
        || flowStatus != ProcessFlowStatus.COMPLETED
        || taskResult instanceof TaskResult.ReturnResult) {
      if (CollectionUtils.isNotEmpty(remainingChildren)) {
        suspendInstances(parentInstance, remainingChildren);
      }
      ExecutionLifecycleManager lifecycleManager =
          getService(concurrentInstance, ExecutionLifecycleManager.class);
      lifecycleManager.signalProcess(
          concurrentInstance,
          parentInstance,
          createParentSignal(flowStatus, taskResult, concurrentInstance));
    }
  }

  @Override
  protected TaskResult doExecute(ProcessInstance processInstance) {
    return TaskResult.Continue.with(Collections.emptyList());
  }

  private void copyVariablesFromConcurrentInstance(
      ProcessInstance concurrentInstance, ProcessInstance parentInstance) {
    VariableStore variableStore = getService(concurrentInstance, VariableStore.class);
    Collection<Variable> variables = variableStore.findVariablesForInstance(concurrentInstance);
    if (CollectionUtils.isNotEmpty(variables)) {
      VariableContainer variableContainer = parentInstance.getVariableContainer();
      for (Variable variable : variables) {
        variableContainer.addNewVariable(
            variable.getTaskId(), variable.getType(), variable.getPayload());
      }
      flushNewVariablesIfAny(parentInstance);
    }
  }

  /**
   * Converts concurrent instance completion status to appropriate parent signal. This method is
   * used during fork-join processing to determine how to signal the parent process when a
   * concurrent (child) instance completes.
   *
   * <p>Signal mapping: - Return result -> Return signal with value (early termination) - Completed
   * -> Resume signal (normal completion) - Failed -> Stop signal with failed status - Suspended ->
   * Stop signal with suspended status
   *
   * @param flowStatus The completion status of the concurrent instance
   * @param taskResult The task result from concurrent instance completion
   * @param childInstance the child instance creating this signal
   * @return Signal appropriate signal for parent process
   * @throws IllegalStateException if flow status is invalid
   */
  private Signal createParentSignal(
      ProcessFlowStatus flowStatus, TaskResult taskResult, ProcessInstance childInstance) {
    if (taskResult instanceof TaskResult.ReturnResult returnResult) {
      return Signal.Return.with(returnResult.getReturnValue());
    }
    return switch (flowStatus) {
      case COMPLETED -> Signal.Resume.with(null);
      case FAILED ->
          Signal.Stop.failed(
              extractExecutionResult(ProcessFlowStatus.FAILED, taskResult, childInstance));
      case SUSPENDED -> Signal.Stop.suspended();
      default -> throw new IllegalStateException("invalid flow status: " + flowStatus);
    };
  }
}
