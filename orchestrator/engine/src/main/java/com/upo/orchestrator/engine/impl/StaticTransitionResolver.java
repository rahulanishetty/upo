/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.upo.orchestrator.api.domain.TaskDefinition;
import com.upo.orchestrator.api.domain.TransitionType;
import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.InputValueResolver;
import com.upo.utilities.filter.impl.FilterEvaluator;

/**
 * Default implementation of TransitionResolver that uses statically defined transitions from
 * TaskDefinition. This implementation is suitable for processes with fixed, predefined flow paths.
 *
 * <p>The resolver simply returns the transitions defined in the task definition, making it ideal
 * for: - Simple linear workflows - Fixed decision trees - Predefined parallel paths - Static
 * process definitions
 */
public class StaticTransitionResolver implements TransitionResolver {

  private final List<Transition> transitions;

  private StaticTransitionResolver(List<Transition> transitions) {
    this.transitions = transitions;
  }

  @Override
  public List<Transition> resolveTransitions(
      TaskRuntime taskRuntime, ProcessInstance instance, TaskResult result) {
    return transitions;
  }

  public static StaticTransitionResolver create(
      TaskDefinition definition, ProcessRuntime processRuntime) {
    List<Transition> transitions = new ArrayList<>();
    for (com.upo.orchestrator.api.domain.Transition nextTransition :
        definition.getNextTransitions()) {
      Transition transition = createTransition(processRuntime, nextTransition);
      transitions.add(transition);
    }
    return new StaticTransitionResolver(transitions);
  }

  private static Transition createTransition(
      ProcessRuntime processRuntime, com.upo.orchestrator.api.domain.Transition transitionDef) {
    return new Transition() {
      @Override
      public TransitionType getType() {
        return transitionDef.getType();
      }

      @Override
      public TaskRuntime getNextTaskRuntime() {
        return processRuntime.getOrCreateTaskRuntime(transitionDef.getNextTaskId());
      }

      @Override
      public Optional<FilterEvaluator<ProcessInstance>> getPredicate() {
        InputValueResolver inputValueResolver =
            processRuntime.getCoreServices().getService(InputValueResolver.class);
        return transitionDef
            .getPredicate()
            .map(
                filter ->
                    ProcessFilterEvaluatorFactory.createEvaluator(filter, inputValueResolver));
      }
    };
  }
}
