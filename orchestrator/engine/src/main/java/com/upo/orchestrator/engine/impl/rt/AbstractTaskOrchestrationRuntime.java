/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.rt;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upo.orchestrator.api.domain.TransitionType;
import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.impl.CompositeVariableView;
import com.upo.orchestrator.engine.impl.ImmutableVariableContainer;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.models.ProcessVariable;
import com.upo.orchestrator.engine.services.*;
import com.upo.orchestrator.engine.utils.ExceptionUtils;
import com.upo.orchestrator.engine.utils.VariableUtils;
import com.upo.utilities.ds.CollectionUtils;
import com.upo.utilities.filter.impl.FilterEvaluator;

/**
 * Abstract base class that implements core task orchestration and execution flow while leaving
 * task-specific execution details to concrete implementations.
 */
public abstract class AbstractTaskOrchestrationRuntime extends AbstractTaskRuntime
    implements TaskRuntime {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractTaskOrchestrationRuntime.class);

  public AbstractTaskOrchestrationRuntime(ProcessRuntime parent, String taskId) {
    super(parent, taskId);
  }

  @Override
  public Next execute(ProcessInstance processInstance) {
    Optional<Next> next = beforeTaskExecution(processInstance);
    if (next.isPresent()) {
     // short-circuited
      return next.get();
    }
    ProcessFlowResult processFlowResult = executeTaskAndResolveFlow(processInstance);
    return afterTaskExecution(processInstance, processFlowResult)
        .orElseGet(() -> onTaskCompletion(processInstance, processFlowResult));
  }

  @Override
  public Next handleSignal(ProcessInstance processInstance, Map<String, Object> payload) {
    return onTaskCompletion(processInstance, null);
  }

  private TaskResult _execute(ProcessInstance processInstance) {
    VariableUtils.loadMissingReferencedVariables(processInstance, dependencies);
    TaskResult taskResult = null;
    try {
      if (skipCondition == null || !skipCondition.evaluate(processInstance)) {
        taskResult = doExecute(processInstance);
      } else {
        taskResult = TaskResult.continueWithVariables(List.of(skippedInput()));
      }
    } catch (Throwable th) {
      taskResult = toError(processInstance, th);
    }
    return taskResult;
  }

  protected abstract TaskResult doExecute(ProcessInstance processInstance);

  /**
   * Executes a task and determines subsequent process flow based on task result and available
   * transitions. This method combines task execution with flow resolution to determine the next
   * steps in the process.
   *
   * @param processInstance Current process instance being executed
   * @return ProcessFlowResult containing task result, flow status, and available transitions
   */
  protected ProcessFlowResult executeTaskAndResolveFlow(ProcessInstance processInstance) {
    TaskResult taskResult = _execute(processInstance);
    return resolveProcessFlow(processInstance, taskResult);
  }

  /**
   * Resolves process flow by evaluating task result and determining available transitions. The
   * resolution process involves: 1. Handling wait states 2. Resolving available transitions 3.
   * Finding matching transition based on task result 4. Determining final process flow status
   *
   * @param processInstance Current process instance
   * @param taskResult Result from task execution
   * @return ProcessFlowResult containing flow resolution details
   */
  private ProcessFlowResult resolveProcessFlow(
      ProcessInstance processInstance, TaskResult taskResult) {
    if (taskResult.getStatus() == TaskResult.Status.WAIT) {
      return new ProcessFlowResult(taskResult, ProcessFlowStatus.WAIT, Collections.emptyList());
    }
   // Resolve all possible transitions
    List<Transition> availableTransitions =
        outgoingTransitions.resolveTransitions(this, processInstance, taskResult);
   // Find the appropriate transition to follow
    Optional<Transition> matchingTransition =
        findMatchingTransition(processInstance, taskResult, availableTransitions);
   // Determine flow status based on matching transition and task result
    ProcessFlowStatus flowStatus = determineFlowStatus(matchingTransition.isPresent(), taskResult);
    return new ProcessFlowResult(taskResult, flowStatus, availableTransitions);
  }

  /**
   * Determines the process flow status based on transition availability and task result. The status
   * hierarchy is: 1. CONTINUE if there's a matching transition 2. FAILED if task resulted in error
   * 3. COMPLETED if no more transitions
   *
   * @param hasMatchingTransition Whether a matching transition was found
   * @param taskResult Result from task execution
   * @return Resolved ProcessFlowStatus
   */
  private static ProcessFlowStatus determineFlowStatus(
      boolean hasMatchingTransition, TaskResult taskResult) {
    if (hasMatchingTransition) {
      return ProcessFlowStatus.CONTINUE;
    }
    return taskResult.getStatus() == TaskResult.Status.ERROR
        ? ProcessFlowStatus.FAILED
        : ProcessFlowStatus.COMPLETED;
  }

  /**
   * Finds the matching transition to follow based on task result and transition conditions. Creates
   * a temporary variable scope that includes task result variables for transition evaluation.
   *
   * @param processInstance Current process instance
   * @param taskResult Result from task execution
   * @param transitions Available transitions to evaluate
   * @return Optional containing the matching transition, empty if no match found
   */
  private static Optional<Transition> findMatchingTransition(
      ProcessInstance processInstance, TaskResult taskResult, List<Transition> transitions) {
    boolean hasTemporaryScope = false;
    VariableContainer original = processInstance.getVariableContainer();
    try {
     // Create temporary variable scope if task produced variables
      if (CollectionUtils.isNotEmpty(taskResult.getVariables())) {
        processInstance.setVariableContainer(
            createTemporaryScopeWithTaskResults(taskResult, original));
        hasTemporaryScope = true;
      }
      Transition matchingTransition =
          evaluateTransitionsForMatch(processInstance, taskResult, transitions);
      return Optional.ofNullable(matchingTransition);
    } finally {
     // Restore original variable scope
      if (hasTemporaryScope) {
        processInstance.setVariableContainer(original);
      }
    }
  }

  /**
   * Evaluates transitions to find the first matching one based on task result and transition type.
   * Evaluation order: 1. For CONTINUE status: evaluates CONDITIONAL transitions, remembers first
   * DEFAULT 2. For ERROR status: evaluates ERROR transitions 3. Returns first matching transition
   * or remembered DEFAULT
   *
   * @param processInstance Current process instance
   * @param taskResult Result from task execution
   * @param transitions Transitions to evaluate
   * @return Matching transition or null if no match found
   */
  private static Transition evaluateTransitionsForMatch(
      ProcessInstance processInstance, TaskResult taskResult, List<Transition> transitions) {
    Transition defaultTransition = null;

    for (Transition transition : transitions) {
      Transition transitionToEvaluate = null;

      if (taskResult.getStatus() == TaskResult.Status.CONTINUE) {
        if (transition.getType() == TransitionType.DEFAULT) {
          defaultTransition = defaultTransition == null ? transition : defaultTransition;
          continue;
        } else if (transition.getType() == TransitionType.CONDITIONAL) {
          transitionToEvaluate = transition;
        }
      } else if (taskResult.getStatus() == TaskResult.Status.ERROR
          && transition.getType() == TransitionType.ERROR) {
        transitionToEvaluate = transition;
      }

      if (transitionToEvaluate != null
          && evaluateTransitionPredicate(processInstance, transitionToEvaluate)) {
        return transition;
      }
    }

    return defaultTransition;
  }

  private static CompositeVariableView createTemporaryScopeWithTaskResults(
      TaskResult taskResult, VariableContainer original) {
    CompositeVariableView view = new CompositeVariableView();
    ImmutableVariableContainer tempContainer = new ImmutableVariableContainer();
    for (Variable variable : taskResult.getVariables()) {
      tempContainer.restoreVariable(
          variable.getTaskId(), variable.getType(), variable.getPayload());
    }
    view.addVariables(tempContainer);
    view.addVariables(original);
    return view;
  }

  private static boolean evaluateTransitionPredicate(
      ProcessInstance processInstance, Transition toEvaluate) {
    Optional<FilterEvaluator<ProcessInstance>> predicate = toEvaluate.getPredicate();
    boolean matched = true;
    if (predicate.isPresent()) {
      matched = predicate.get().evaluate(processInstance);
    }
    return matched;
  }

  protected void executeAfterSave(
      ProcessInstance processInstance, Map<String, Object> afterSaveCommand) {
    if (afterSaveCommand != null) {
      throw new IllegalStateException("this should be overridden where after save command exists!");
    }
  }

  protected TaskResult toError(ProcessInstance processInstance, Throwable throwable) {
    TaskResult taskResult = new TaskResult(TaskResult.Status.ERROR);
    if (throwable instanceof TaskExecutionException taskExecutionException) {
      taskResult.setVariables(taskExecutionException.getVariables());
    } else {
      Map<String, Object> payload = new HashMap<>(toErrorDetails(throwable));
      payload.put("rootCause", toErrorDetails(ExceptionUtils.getRootCause(throwable)));
      ProcessVariable processVariable = toVariable(processInstance, Variable.Type.ERROR, payload);
      taskResult.setVariables(List.of(processVariable));
    }
    return taskResult;
  }

  protected Map<String, Object> toErrorDetails(Throwable throwable) {
    if (throwable == null) {
      return null;
    }
    Map<String, Object> errorDetails = new HashMap<>();
    errorDetails.put("failureClass", throwable.getClass().getSimpleName());
    errorDetails.put("message", throwable.getMessage());
    return errorDetails;
  }

  private Optional<Next> beforeTaskExecution(ProcessInstance processInstance) {
    ProcessServices processServices = getServices(processInstance);
    EnvironmentProvider environmentProvider = processServices.getService(EnvironmentProvider.class);
    if (environmentProvider.isShutdownInProgress()) {
     // check if shutdown is in progress, if yes throw an event to resume from this task
      processInstance.setStatus(ProcessFlowStatus.WAIT);
      if (saveProcessInstance(processInstance, ProcessFlowStatus.CONTINUE)) {
        ExecutionLifecycleManager executionLifecycleManager =
            processServices.getService(ExecutionLifecycleManager.class);
        executionLifecycleManager.resumeProcessFromTask(processInstance, this);
      }
      return Optional.of(Next.EMPTY);
    }
    if (Objects.equals(taskId, processInstance.getTerminateAtTaskId())) {
      handleInstanceCompletion(processInstance, "TERMINATED_AT_TASK_ID");
      return Optional.of(Next.EMPTY);
    }
    processInstance.setPrevTaskId(processInstance.getCurrTaskId());
    processInstance.setCurrTaskId(taskId);
    processInstance.setCurrentTaskStartTime(System.currentTimeMillis());
    if (processInstance.incrementTaskCount() % 100 == 0) {
     // periodically flush accumulated variables and state to store
      if (!saveProcessInstance(processInstance, ProcessFlowStatus.CONTINUE)) {
        flushNewVariablesIfAny(processInstance);
        return Optional.of(Next.EMPTY);
      }
    }
    ExecutionLifecycleAuditor lifecycleAuditor =
        processServices.getService(ExecutionLifecycleAuditor.class);
    lifecycleAuditor.beforeExecution(this, processInstance);
    return Optional.empty();
  }

  private void handleInstanceCompletion(ProcessInstance processInstance, String code) {}

  private Optional<Next> afterTaskExecution(
      ProcessInstance processInstance, ProcessFlowResult processFlowResult) {
    processInstance.setCurrentTaskEndTime(System.currentTimeMillis());
    applyResultOnProcessInstance(processInstance, processFlowResult);
    if (processFlowResult.getFlowStatus() != ProcessFlowStatus.CONTINUE) {
      if (!saveProcessInstance(processInstance, ProcessFlowStatus.CONTINUE)) {
        flushNewVariablesIfAny(processInstance);
        LOGGER.error("somethings wrong, execution instance not in expected state");
        return Optional.of(Next.EMPTY);
      } else {
        Map<String, Object> afterSaveCommand =
            processFlowResult.getTaskResult().getAfterSaveCommand();
        if (afterSaveCommand != null) {
          executeAfterSave(processInstance, afterSaveCommand);
        }
      }
    }
    ExecutionLifecycleAuditor lifecycleAuditor =
        getServices(processInstance).getService(ExecutionLifecycleAuditor.class);
    lifecycleAuditor.afterExecution(this, processInstance);
    return Optional.empty();
  }

  private static void applyResultOnProcessInstance(
      ProcessInstance processInstance, ProcessFlowResult processFlowResult) {
    TaskResult taskResult = processFlowResult.getTaskResult();
    processInstance.setStatus(processFlowResult.getFlowStatus());
    if (CollectionUtils.isNotEmpty(taskResult.getVariables())) {
      VariableContainer variableContainer = processInstance.getVariableContainer();
      for (Variable variable : taskResult.getVariables()) {
        variableContainer.addNewVariable(
            variable.getTaskId(), variable.getType(), variable.getPayload());
      }
    }
  }

  private Next onTaskCompletion(
      ProcessInstance processInstance, ProcessFlowResult processFlowResult) {
    processInstance.setCurrentTaskEndTime(System.currentTimeMillis());
    ExecutionLifecycleAuditor lifecycleAuditor =
        getServices(processInstance).getService(ExecutionLifecycleAuditor.class);
    lifecycleAuditor.onCompletion(this, processInstance);
    return processFlowResult::getNextTransitions;
  }

  protected void flushNewVariablesIfAny(ProcessInstance processInstance) {
    VariableContainer variableContainer = processInstance.getVariableContainer();
    List<ProcessVariable> newVariables = variableContainer.getNewVariables();
    if (CollectionUtils.isEmpty(newVariables)) {
      return;
    }
    ProcessServices processServices = getServices(processInstance);
    VariableStore variableStore = processServices.getService(VariableStore.class);
    for (ProcessVariable newVariable : newVariables) {
      newVariable.initId(processInstance);
    }
    variableStore.saveMany(newVariables);
    variableContainer.clearNewVariables();
  }

  protected boolean saveProcessInstance(
      ProcessInstance processInstance, ProcessFlowStatus expectedStatus) {
    ProcessServices processServices = getServices(processInstance);
    ProcessInstanceStore instanceStore = processServices.getService(ProcessInstanceStore.class);
    if (instanceStore.save(processInstance, expectedStatus)) {
      processInstance.setTaskCountSinceLastFlush(0L);
      flushNewVariablesIfAny(processInstance);
      return true;
    }
    return false;
  }

  private Variable skippedInput() {
    ProcessVariable variable = new ProcessVariable();
    variable.setTaskId(taskId);
    variable.setType(Variable.Type.INPUT);
    variable.setPayload(Map.of("skipped", "true"));
    return variable;
  }
}
