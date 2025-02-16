/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.rt;

import java.io.Closeable;
import java.util.*;

import com.upo.orchestrator.api.domain.TransitionType;
import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.utilities.ds.CollectionUtils;
import com.upo.utilities.ds.IOUtils;
import com.upo.utilities.ds.Pair;

public abstract class LoopTaskRuntime extends AbstractTaskOrchestrationRuntime {

  public LoopTaskRuntime(ProcessRuntime parent, String taskId) {
    super(parent, taskId);
  }

  @Override
  public void setInputs(Object inputs) {
    super.setInputs(inputs);
    this.dependencies = CollectionUtils.nullSafeMutableSet(dependencies);
    this.dependencies.add(Pair.of(taskId, Variable.Type.STATE));
  }

  @Override
  protected TaskResult doExecute(ProcessInstance processInstance) {
    List<Variable> variables = new ArrayList<>();
   // Get or initialize iteration state
    Iterator<?> iterator = getOrCreateIterator(processInstance, variables);
    if (iterator == null) {
      return handleIterationComplete(processInstance, null, variables);
    }

   // Process next iteration if available
    if (iterator.hasNext()) {
      return processNextItem(processInstance, iterator, variables);
    }

   // Handle iteration completion
    return handleIterationComplete(processInstance, iterator, variables);
  }

  protected abstract Object updateState(
      Object existingState, ProcessInstance processInstance, Object nextItem);

  protected abstract Iterator<?> buildIterator(
      Object state, ProcessInstance processInstance, Object processedInputs);

  /** Retrieves existing iterator or creates a new one based on current state. */
  private Iterator<?> getOrCreateIterator(
      ProcessInstance processInstance, List<Variable> variables) {
    VariableContainer variableContainer = processInstance.getVariableContainer();
    Iterator<?> iterator =
        (Iterator<?>) variableContainer.getVariable(taskId, Variable.Type.TRANSIENT);
    if (iterator != null) {
      return iterator;
    }

    Object state = variableContainer.getVariable(taskId, Variable.Type.STATE);
    Object processedInputs = inputs.evaluate(processInstance);

    iterator = buildIterator(state, processInstance, processedInputs);
    if (iterator != null) {
      variables.add(toTransientVariable(iterator));
    }
    return iterator;
  }

  /** Handles completion of iteration by finding the default transition. */
  private TaskResult handleIterationComplete(
      ProcessInstance processInstance, Iterator<?> iterator, List<Variable> variables) {
    if (iterator instanceof Closeable closeable) {
      IOUtils.closeQuietly(closeable);
    }
    variables.add(toTransientVariable(null));
    variables.add(toStateVariable(null));
    return TaskResult.ContinueWithTransitions.with(
        variables, findCompletionTransition(processInstance));
  }

  /**
   * Processes the next item in the iteration and determines the next transition. If no conditional
   * transition matches (conditions evaluate to false), moves to completion by using the default
   * transition.
   */
  private TaskResult processNextItem(
      ProcessInstance processInstance, Iterator<?> iterator, List<Variable> variables) {

    List<Variable> newVariables = CollectionUtils.nullSafeMutableList(variables);
   // Process next item
    Object nextItem = iterator.next();
    newVariables.add(toOutputVariable(nextItem));

   // Update iteration state
    Object currentState =
        processInstance.getVariableContainer().getVariable(taskId, Variable.Type.STATE);
    Object newState = updateState(currentState, processInstance, nextItem);
    newVariables.add(toStateVariable(newState));

   // Find next transition
    List<Transition> nextTransition = findIterationTransition(processInstance, newVariables);
    if (nextTransition.isEmpty()) {
      return handleIterationComplete(processInstance, iterator, variables);
    }
    return TaskResult.ContinueWithTransitions.with(newVariables, nextTransition);
  }

  /**
   * Finds the appropriate transition based on current iteration variables. Creates a temporary
   * variable scope to evaluate transition conditions.
   */
  private List<Transition> findIterationTransition(
      ProcessInstance processInstance, List<Variable> iterationVariables) {
    Transition transition = evaluateTransitions(processInstance, iterationVariables);
    return transition != null ? Collections.singletonList(transition) : Collections.emptyList();
  }

  /** Evaluates transitions using a temporary variable scope. */
  private Transition evaluateTransitions(
      ProcessInstance processInstance, List<Variable> variables) {
    List<Transition> transitions =
        resolveTransitions(processInstance, TaskResult.Continue.with(Collections.emptyList()));

    if (CollectionUtils.isEmpty(transitions)) {
      return null;
    }

    VariableContainer original = processInstance.getVariableContainer();
    try {
      processInstance.setVariableContainer(createTemporaryScopeWithVariables(original, variables));
      return findMatchingTransition(processInstance, transitions);
    } finally {
      processInstance.setVariableContainer(original);
    }
  }

  /** Finds the first matching conditional transition or returns null. */
  private Transition findMatchingTransition(
      ProcessInstance processInstance, List<Transition> transitions) {
    return transitions.stream()
        .filter(t -> t.getType() == TransitionType.CONDITIONAL)
        .filter(t -> evaluateTransitionPredicate(processInstance, t))
        .findFirst()
        .orElse(null);
  }

  /** Finds the default transition for iteration completion. */
  public List<Transition> findCompletionTransition(ProcessInstance processInstance) {
    return resolveTransitions(processInstance, TaskResult.Continue.with(Collections.emptyList()))
        .stream()
        .filter(t -> t.getType() == TransitionType.DEFAULT)
        .findFirst()
        .map(Collections::singletonList)
        .orElse(Collections.emptyList());
  }
}
