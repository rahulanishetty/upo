/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.rt;

import static com.upo.orchestrator.engine.impl.callbacks.StartForkedInstancesProcessInstanceCallbackBuilder.*;

import java.util.*;

import com.upo.orchestrator.api.domain.TransitionType;
import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.impl.ProcessExecutorImpl;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.ExecutionLifecycleManager;
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
public class ForkTransitionsRuntime extends AbstractTaskOrchestrationRuntime {

  private String joinTaskId;
  private TransitionResolver nextTransitionsResolver;

  public ForkTransitionsRuntime(ProcessRuntime parent, String taskId) {
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

  public void setJoinTaskId(String joinTaskId) {
    this.joinTaskId = joinTaskId;
  }

  @Override
  public void setOutgoingTransitions(TransitionResolver outgoingTransitions) {
    nextTransitionsResolver = outgoingTransitions;
  }

  @Override
  protected TaskResult doExecute(ProcessInstance processInstance) {
    List<Transition> matchingTransitions = findTransitionsToFork(processInstance);

    Map<String, Object> processedInputs = inputs.evaluate(processInstance);
    boolean waitForChildren =
        joinTaskId != null && !(boolean) processedInputs.getOrDefault("noWait", false);

    List<Variable> variables = Collections.singletonList(toInputVariable(processedInputs));

    if (shouldExecuteSequentially(matchingTransitions, waitForChildren)) {
      return TaskResult.ContinueWithTransitions.with(variables, matchingTransitions);
    }

    List<ProcessInstance> forkedInstances =
        createAndSaveForkedInstances(processInstance, matchingTransitions);

    if (waitForChildren) {
     // Synchronous execution with join point
      Map<String, Object> callbackData = createCallbackData(forkedInstances);
      return new TaskResult.Wait(variables, TYPE, callbackData);
    }

   // Asynchronous execution - start all instances and continue
    startForkedInstances(processInstance, forkedInstances);
    return TaskResult.Continue.with(variables);
  }

  /**
   * Determines if execution should proceed sequentially instead of forking. Sequential execution is
   * chosen when: 1. There's only 0 or 1 transition to execute, AND 2. A join point is specified,
   * AND 3. We are waiting for child completions
   *
   * @param transitions The transitions to be executed
   * @param waitForChildren Whether parent should wait for child completions
   * @return true if execution should proceed sequentially, false if parallel execution is needed
   */
  private boolean shouldExecuteSequentially(List<Transition> transitions, boolean waitForChildren) {
    return transitions.size() <= 1 && waitForChildren;
  }

  private List<Transition> resolveTransitions(ProcessInstance processInstance) {
    if (nextTransitionsResolver == null) {
      return Collections.emptyList();
    }
    return nextTransitionsResolver.resolveTransitions(this, processInstance, null);
  }

  private void startForkedInstances(
      ProcessInstance parentInstance, List<ProcessInstance> forkedInstances) {
    ExecutionLifecycleManager lifecycleManager =
        getService(parentInstance, ExecutionLifecycleManager.class);
    for (ProcessInstance forkedInstance : forkedInstances) {
      lifecycleManager.continueProcessFromTask(
          forkedInstance.getId(), forkedInstance.getCurrTaskId());
    }
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
    List<String> waitOnInstanceIds = new ArrayList<>();
    for (ProcessInstance concurrentInstance : concurrentInstances) {
      instances.add(
          Map.of(
              TASK_ID,
              concurrentInstance.getCurrTaskId(),
              INSTANCE_ID,
              concurrentInstance.getId()));
      waitOnInstanceIds.add(concurrentInstance.getId());
    }
    return Map.of(INSTANCES, instances, WAIT_ON_INSTANCE_IDS, waitOnInstanceIds);
  }

  /** Creates a child concurrent process instance. */
  private ProcessInstance createChildConcurrentInstance(
      ProcessInstance parentInstance, String startFromTaskId) {
    ProcessInstance concurrentInstance =
        ProcessExecutorImpl.createChildInstance(parentInstance, parentInstance.toProcessDetails());
    concurrentInstance.setConcurrent(true);
    concurrentInstance.setTerminateAtTaskId(joinTaskId);
    concurrentInstance.setCurrTaskId(startFromTaskId);
    return concurrentInstance;
  }

  /** Evaluates transitions to determine which paths should be forked. */
  private List<Transition> findTransitionsToFork(ProcessInstance processInstance) {
    List<Transition> availableTransitions = resolveTransitions(processInstance);

    Transition defaultTransition = null;
    List<Transition> matchingTransitions = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(availableTransitions)) {
      for (Transition transition : availableTransitions) {
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
