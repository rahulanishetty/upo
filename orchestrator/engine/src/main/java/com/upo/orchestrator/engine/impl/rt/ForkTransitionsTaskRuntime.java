/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.rt;

import static com.upo.orchestrator.engine.impl.StartForkedInstancesProcessInstanceCallbackBuilderImpl.*;

import java.util.*;

import com.upo.orchestrator.api.domain.TransitionType;
import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.impl.ProcessExecutorImpl;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.ProcessInstanceStore;
import com.upo.utilities.ds.CollectionUtils;

/**
 * Task runtime implementation that handles parallel process execution through forking. This runtime
 * evaluates transitions and creates concurrent process instances for parallel execution, optionally
 * synchronizing them at a join point.
 *
 * <p>Execution flow: 1. Evaluates transitions to determine which paths to fork 2. Creates
 * concurrent child instances for each matching transition 3. Sets up join synchronization if
 * configured 4. Persists forked instances 5. Sets up callback mechanism for join coordination
 */
public class ForkTransitionsTaskRuntime extends AbstractTaskOrchestrationRuntime {

  private String joinTaskId;
  private TransitionResolver nextTransitionsResolver;

  public ForkTransitionsTaskRuntime(ProcessRuntime parent, String taskId) {
    super(parent, taskId);
    setOutgoingTransitions(
        (_, _, taskResult) -> {
          if (taskResult instanceof TaskResult.ContinueWithTransitions continueWithTransitions) {
            return continueWithTransitions.getTransitions();
          }
          if (joinTaskId == null) {
            return Collections.emptyList();
          }
          return List.of(
              Transition.defaultTransition(() -> parent.getOrCreateTaskRuntime(joinTaskId)));
        });
  }

  public String getJoinTaskId() {
    return joinTaskId;
  }

  public void setJoinTaskId(String joinTaskId) {
    this.joinTaskId = joinTaskId;
  }

  @Override
  public void setOutgoingTransitions(TransitionResolver outgoingTransitions) {
    nextTransitionsResolver = outgoingTransitions;
  }

  @Override
  protected TaskResult doExecute(ProcessInstance processInstance) {
    List<Transition> transitions = Collections.emptyList();
    if (nextTransitionsResolver != null) {
      transitions = nextTransitionsResolver.resolveTransitions(this, processInstance, null);
    }

    List<Transition> matchingTransitions = findTransitionsToFork(processInstance, transitions);
    if (matchingTransitions.size() <= 1) {
     // Optimization: No need to fork for 0 or 1 transitions
      return TaskResult.ContinueWithTransitions.with(Collections.emptyList(), matchingTransitions);
    }

    List<ProcessInstance> concurrentInstances =
        createAndSaveForkedInstances(processInstance, matchingTransitions);

    Map<String, Object> callbackData = createCallbackData(concurrentInstances);
    if (joinTaskId != null) {
      List<String> concurrentInstanceIds =
          CollectionUtils.transformToList(concurrentInstances, ProcessInstance::getId);
      processInstance.setRemainingChildInstances(concurrentInstanceIds);
      return new TaskResult.Wait(Collections.emptyList(), TYPE, callbackData);
    }
    return TaskResult.Continue.with(Collections.emptyList());
  }

  /** Creates and persists forked process instances. */
  private List<ProcessInstance> createAndSaveForkedInstances(
      ProcessInstance processInstance, List<Transition> matchingTransitions) {
    List<ProcessInstance> concurrentInstances = new ArrayList<>();
    for (Transition matchingTransition : matchingTransitions) {
      String taskId = matchingTransition.getNextTaskRuntime().getTaskId();
      concurrentInstances.add(createChildConcurrentInstance(processInstance, taskId));
    }
    ProcessInstanceStore processInstanceStore =
        getService(processInstance, ProcessInstanceStore.class);
    if (!processInstanceStore.saveMany(concurrentInstances)) {
      throw new IllegalStateException("failed to save concurrent instances!");
    }
    return concurrentInstances;
  }

  /** Creates callback data for starting forked instances and synchronization. */
  private static Map<String, Object> createCallbackData(List<ProcessInstance> concurrentInstances) {
    List<Map<String, Object>> instances = new ArrayList<>();
    List<String> remainingInstanceIds = new ArrayList<>();
    for (ProcessInstance concurrentInstance : concurrentInstances) {
      instances.add(
          Map.of(
              TASK_ID,
              concurrentInstance.getCurrTaskId(),
              INSTANCE_ID,
              concurrentInstance.getId()));
      remainingInstanceIds.add(concurrentInstance.getId());
    }
    return Map.of(INSTANCES, instances, WAIT_ON_INSTANCE_IDS, remainingInstanceIds);
  }

  /** Creates a child concurrent process instance. */
  private ProcessInstance createChildConcurrentInstance(
      ProcessInstance parentInstance, String startFromTaskId) {
    ProcessInstance concurrentInstance = ProcessExecutorImpl.createChildInstance(parentInstance);
    concurrentInstance.setConcurrent(true);
    concurrentInstance.setTerminateAtTaskId(joinTaskId);
    concurrentInstance.setCurrTaskId(startFromTaskId);
    return concurrentInstance;
  }

  /** Evaluates transitions to determine which paths should be forked. */
  private static List<Transition> findTransitionsToFork(
      ProcessInstance processInstance, List<Transition> transitions) {
    Transition defaultTransition = null;
    List<Transition> matchingTransitions = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(transitions)) {
      for (Transition transition : transitions) {
        if (transition.getType() == TransitionType.DEFAULT) {
          defaultTransition = transition;
        } else if (transition.getType() == TransitionType.CONDITIONAL) {
          if (evaluateTransitionPredicate(processInstance, transition)) {
            matchingTransitions.add(transition);
          }
        }
      }
    }
    if (matchingTransitions.isEmpty() && defaultTransition != null) {
      matchingTransitions.add(defaultTransition);
    }
    return matchingTransitions;
  }
}
