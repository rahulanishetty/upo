/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.rt;

import java.util.*;

import com.upo.orchestrator.api.domain.TransitionType;
import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.utilities.ds.CollectionUtils;

/**
 * Runtime implementation for handling conditional flow control in process orchestration. This class
 * evaluates conditions and determines the appropriate transition path for process execution.
 *
 * @see AbstractTaskOrchestrationRuntime
 * @see ProcessRuntime
 */
public class ConditionalTransitionTaskRuntime extends AbstractTaskOrchestrationRuntime {

  public ConditionalTransitionTaskRuntime(ProcessRuntime parent, String taskId) {
    super(parent, taskId);
  }

  /**
   * Overrides the transition resolution strategy to handle special cases of conditional execution
   * results.
   *
   * @param outgoingTransitions The base transition resolver
   */
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

  /**
   * Executes the conditional logic for this task, evaluating transitions and determining the next
   * execution path.
   *
   * <p>Execution flow: 1. Resolves all possible transitions 2. Identifies default transition (if
   * any) 3. Evaluates conditional transitions in order until a match is found 4. Falls back to
   * default transition if no conditions match 5. Produces output variable indicating the evaluation
   * result
   *
   * @param processInstance Current process instance containing execution state
   * @return TaskResult containing the selected transition and output variables
   */
  @Override
  protected TaskResult doExecute(ProcessInstance processInstance) {
    List<Transition> transitions =
        outgoingTransitions.resolveTransitions(this, processInstance, null);
    Transition defaultTransition = null;
    Transition matchingTransition = null;
    if (CollectionUtils.isNotEmpty(transitions)) {
      for (Transition transition : transitions) {
        if (transition.getType() == TransitionType.DEFAULT) {
          defaultTransition = transition;
        } else if (transition.getType() == TransitionType.CONDITIONAL) {
          if (evaluateTransitionPredicate(processInstance, transition)) {
            matchingTransition = transition;
            break;
          }
        }
      }
    }
    if (matchingTransition == null) {
      matchingTransition = defaultTransition;
    }
    return TaskResult.ContinueWithTransitions.with(
        Collections.emptyList(),
        matchingTransition != null
            ? Collections.singletonList(matchingTransition)
            : Collections.emptyList());
  }
}
