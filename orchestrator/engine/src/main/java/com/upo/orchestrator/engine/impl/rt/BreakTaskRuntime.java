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
 * Task runtime that provides loop break functionality based on conditions. When condition matches,
 * execution breaks from loop and follows completion path.
 */
public class BreakTaskRuntime extends AbstractTaskOrchestrationRuntime {

  private String loopTaskId;
  private FilterEvaluator<ProcessInstance> breakCondition;

  public BreakTaskRuntime(ProcessRuntime parent, String taskId) {
    super(parent, taskId);
  }

  public void setLoopTaskId(String loopTaskId) {
    this.loopTaskId = loopTaskId;
  }

  public void setBreakCondition(FilterEvaluator<ProcessInstance> breakCondition) {
    this.breakCondition = breakCondition;
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
    Map<String, Object> breakState = new HashMap<>();
    if (shouldBreakLoop(processInstance)) {
      breakState.put("break", true);
      LoopTaskRuntime loopTaskRuntime = (LoopTaskRuntime) parent.getOrCreateTaskRuntime(loopTaskId);
      List<Transition> completionTransition =
          loopTaskRuntime.findCompletionTransition(processInstance);
      return TaskResult.ContinueWithTransitions.with(
          Collections.singletonList(toBreakStateVariable(breakState)), completionTransition);
    }
    breakState.put("break", false);
    return TaskResult.Continue.with(Collections.singletonList(toBreakStateVariable(breakState)));
  }

  private boolean shouldBreakLoop(ProcessInstance processInstance) {
    return breakCondition == null || breakCondition.evaluate(processInstance);
  }

  private Variable toBreakStateVariable(Map<String, Object> breakState) {
    return toOutputVariable(breakState);
  }
}
