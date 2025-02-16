/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.rt;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.utilities.filter.impl.FilterEvaluator;

/**
 * Task runtime that provides loop continue functionality based on conditions. When condition
 * matches, skips to the loop task to begin next iteration.
 */
public class ContinueTaskRuntime extends AbstractTaskOrchestrationRuntime {
  private String loopTaskId;
  private FilterEvaluator<ProcessInstance> continueCondition;

  public ContinueTaskRuntime(ProcessRuntime parent, String taskId) {
    super(parent, taskId);
  }

  public void setLoopTaskId(String loopTaskId) {
    this.loopTaskId = loopTaskId;
  }

  public void setContinueCondition(FilterEvaluator<ProcessInstance> continueCondition) {
    this.continueCondition = continueCondition;
  }

  @Override
  public void setOutgoingTransitions(TransitionResolver outgoingTransitions) {
    super.setOutgoingTransitions(
        (taskRuntime, instance, result) -> {
          if (result instanceof TaskResult.ContinueWithTransitions continueWithTransitions) {
            return continueWithTransitions.getTransitions();
          }
          return outgoingTransitions.resolveTransitions(taskRuntime, instance, result);
        });
  }

  @Override
  protected TaskResult doExecute(ProcessInstance processInstance) {
    Map<String, Object> continueState = new HashMap<>();
    if (shouldContinueLoop(processInstance)) {
      continueState.put("continue", true);
      return TaskResult.ContinueWithTransitions.with(
          Collections.singletonList(toContinueStateVariable(continueState)),
          List.of(Transition.defaultTransition(() -> parent.getOrCreateTaskRuntime(loopTaskId))));
    }
    continueState.put("continue", false);
    return TaskResult.Continue.with(
        Collections.singletonList(toContinueStateVariable(continueState)));
  }

  private boolean shouldContinueLoop(ProcessInstance processInstance) {
    return continueCondition == null || continueCondition.evaluate(processInstance);
  }

  private Variable toContinueStateVariable(Map<String, Object> continueState) {
    return toOutputVariable(continueState);
  }
}
