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

public class JoinTransitionsTaskRuntime extends AbstractTaskOrchestrationRuntime
    implements ForkJoinRuntime {

  public JoinTransitionsTaskRuntime(ProcessRuntime parent, String taskId) {
    super(parent, taskId);
  }

  @Override
  public void join(
      ProcessInstance concurrentInstance,
      ProcessInstance parentInstance,
      ProcessFlowStatus flowStatus) {
    ProcessInstanceStore processInstanceStore =
        getService(concurrentInstance, ProcessInstanceStore.class);
    if (!processInstanceStore.removeCompletedInstanceId(
        parentInstance, concurrentInstance.getId())) {
      return;
    }
   // Copy variables from completed instance to parent
    copyVariablesFromConcurrentInstance(concurrentInstance, parentInstance);
    Set<String> remainingChildren = processInstanceStore.getRemainingChildren(parentInstance);

   // Signal parent if:
   // 1. No remaining children, OR
   // 2. Non-successful completion with remaining children (need to suspend them)
    if (CollectionUtils.isEmpty(remainingChildren) || flowStatus != ProcessFlowStatus.COMPLETED) {
      if (CollectionUtils.isNotEmpty(remainingChildren)) {
        suspendInstances(parentInstance, remainingChildren);
      }
      ExecutionLifecycleManager lifecycleManager =
          getService(concurrentInstance, ExecutionLifecycleManager.class);
      lifecycleManager.signalProcess(concurrentInstance, parentInstance, flowStatus, null);
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
}
