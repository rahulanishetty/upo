/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.*;
import java.util.function.Function;

import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.models.ProcessEnv;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.EnvironmentProvider;
import com.upo.orchestrator.engine.services.ProcessInstanceStore;
import com.upo.orchestrator.engine.services.ProcessServices;
import com.upo.orchestrator.engine.utils.ProcessUtils;
import com.upo.utilities.context.RequestContext;
import com.upo.utilities.filter.impl.FilterEvaluator;
import com.upo.utilities.ulid.UlidUtils;

public class ProcessExecutorImpl implements ProcessExecutor {

  private final ProcessServices processServices;
  private final ProcessRuntime processRuntime;
  private final ExecutionStrategy strategy;

  public ProcessExecutorImpl(
      ProcessServices processServices, ProcessRuntime processRuntime, ExecutionStrategy strategy) {
    this.processServices = processServices;
    this.processRuntime = processRuntime;
    this.strategy = strategy;
  }

  @Override
  public ProcessServices getServices() {
    return processServices;
  }

  @Override
  public ProcessRuntime getRuntime() {
    return processRuntime;
  }

  @Override
  public String start(Map<String, Object> payload) {
    Optional<FilterEvaluator<Map<String, Object>>> predicate = processRuntime.getPredicate();
    if (predicate.isPresent()) {
      if (!predicate.get().evaluate(payload)) {
        return null;
      }
    }
    ProcessInstance processInstance = createProcessInstance();
    if (processInstance == null) {
      throw new IllegalStateException("failed to create process instance");
    }
    startInternal(processInstance, payload);
    return processInstance.getId();
  }

  @Override
  public void start(String instanceId, Map<String, Object> payload) {
    ProcessInstance processInstance = findProcessInstance(instanceId);
    startInternal(processInstance, payload);
  }

  @Override
  public void signal(String processInstanceId, Signal signal) {
    ProcessInstanceStore instanceStore = processServices.getService(ProcessInstanceStore.class);
    ProcessInstance processInstance = lookupProcessInstance(processInstanceId, instanceStore);
    executeTaskSequence(
        processInstance,
        processInstance.getCurrTaskId(),
        (taskRuntime) -> taskRuntime.handleSignal(processInstance, signal));
  }

  private void startInternal(ProcessInstance processInstance, Map<String, Object> payload) {
    processInstance.setInput(payload);
    executeTaskSequence(
        processInstance,
        processRuntime.getDefinition().getStartTaskId(),
        (task) -> task.execute(processInstance));
  }

  private ProcessInstance findProcessInstance(String instanceId) {
    ProcessInstanceStore instanceStore = processServices.getService(ProcessInstanceStore.class);
    ProcessInstance processInstance =
        instanceStore
            .findById(instanceId)
            .orElseThrow(() -> new IllegalStateException("failed to create process instance"));
    ProcessEnv processEnv = processInstance.getProcessEnv();
    processEnv.setProcessServices(getServices());
    processInstance.setVariableContainer(createVariableContainer(processEnv));
    return processInstance;
  }

  private ProcessInstance lookupProcessInstance(
      String processInstanceId, ProcessInstanceStore instanceStore) {
    Optional<ProcessInstance> processInstance =
        instanceStore.findById(processInstanceId, ProcessFlowStatus.WAIT);
    return processInstance
        .map(
            instance -> {
              ProcessEnv processEnv = instance.getProcessEnv();
              if (processEnv == null) {
                throw new IllegalStateException("ProcessEnv is null, but it shouldn't be!");
              }
              processEnv.setProcessServices(processServices);
              instance.setVariableContainer(createVariableContainer(processEnv));
              return instance;
            })
        .orElseThrow(
            () ->
                new IllegalStateException("process no longer in expected state to send a signal"));
  }

  /**
   * Executes tasks in sequence based on their transitions. Manages the flow of execution from one
   * task to the next, following the transition paths returned by each task.
   *
   * @param processInstance current process instance
   * @param taskId initial task to execute
   * @param initiator function to initiate task execution
   */
  private void executeTaskSequence(
      ProcessInstance processInstance,
      String taskId,
      Function<TaskRuntime, TaskRuntime.Next> initiator) {
    TaskRuntime taskRuntime = processRuntime.getOrCreateTaskRuntime(taskId);
    TaskRuntime.Next nextTransitions = initiator.apply(taskRuntime);

    Queue<TaskRuntime> taskRuntimes = new LinkedList<>();
    addNextTransitionsToQueue(nextTransitions, taskRuntimes);

    while (!taskRuntimes.isEmpty()) {
      TaskRuntime task = taskRuntimes.poll();
      nextTransitions = task.execute(processInstance);
      addNextTransitionsToQueue(nextTransitions, taskRuntimes);
    }
  }

  /**
   * Adds next tasks to the execution queue based on transitions. Suggested rename: queueNextTasks
   *
   * @param nextTransitions transitions returned by task execution
   * @param taskRuntimes queue of tasks to execute
   */
  private static void addNextTransitionsToQueue(
      TaskRuntime.Next nextTransitions, Queue<TaskRuntime> taskRuntimes) {
    List<Transition> transitions = nextTransitions.transitions();
    if (transitions == null) {
      return;
    }
    for (Transition transition : transitions) {
      taskRuntimes.offer(transition.getNextTaskRuntime());
    }
  }

  /**
   * Creates and initializes a new process instance. Sets up instance metadata, timing information,
   * and environment.
   *
   * @return initialized process instance, or null if save fails
   */
  private ProcessInstance createProcessInstance() {
    ProcessInstance processInstance = new ProcessInstance();
    processInstance.setId(UlidUtils.createId());
    processInstance.setStartTime(System.currentTimeMillis());
    processInstance.setTaskCount(0L);
    processInstance.setStatus(ProcessFlowStatus.CONTINUE);

    ProcessDetails details = processRuntime.getDetails();
    processInstance.setProcessId(details.getId());
    processInstance.setProcessSnapshotId(details.getSnapshotId());
    processInstance.setProcessVersion(details.getSnapshotVersion());
    processInstance.setExecutionStrategy(strategy.name());

    ProcessEnv processEnv = createProcessEnv();
    processInstance.setProcessEnv(processEnv);
    processInstance.setVariableContainer(createVariableContainer(processEnv));
    ProcessInstanceStore instanceStore = processServices.getService(ProcessInstanceStore.class);
    if (!instanceStore.save(processInstance)) {
      return null;
    }
    return processInstance;
  }

  public static ProcessInstance createChildInstance(
      ProcessInstance parentInstance, ProcessDetails processDetails) {
    ProcessInstance childInstance = new ProcessInstance();
    childInstance.setId(UlidUtils.createId());
    childInstance.setStartTime(System.currentTimeMillis());
    childInstance.setTaskCount(0L);
    childInstance.setStatus(ProcessFlowStatus.CONTINUE);

    childInstance.setRootId(ProcessUtils.getRootInstanceId(parentInstance));
    childInstance.setParentId(parentInstance.getId());

    childInstance.setProcessId(processDetails.getId());
    childInstance.setProcessSnapshotId(processDetails.getSnapshotId());
    childInstance.setProcessVersion(processDetails.getSnapshotVersion());

    childInstance.setExecutionStrategy(parentInstance.getExecutionStrategy());
    ProcessEnv childProcessEnv = parentInstance.getProcessEnv().copy();
    childInstance.setProcessEnv(childProcessEnv);

    childInstance.setVariableContainer(createVariableContainer(childProcessEnv));

    return childInstance;
  }

  /**
   * Creates a new VariableContainer instance initialized with process environment data. Maps
   * environment variables, context, and session data making them available for resolution in
   * JsonPath expressions during task execution.
   *
   * @param processEnv process environment containing initial variables
   * @return initialized Variables instance with process environment data
   */
  public static VariableContainer createVariableContainer(ProcessEnv processEnv) {
    VariableContainerImpl variables = new VariableContainerImpl();
    variables.addProcessEnvVariables(processEnv);
    return variables;
  }

  /**
   * Creates process environment with required configuration. Initializes environment variables,
   * request context, and services.
   *
   * @return configured process environment
   */
  private ProcessEnv createProcessEnv() {
    ProcessEnv processEnv = new ProcessEnv();
    Map<String, Object> envVariables =
        processServices.getService(EnvironmentProvider.class).lookupEnvVariables();
    processEnv.setEnv(envVariables);
    RequestContext requestContext = RequestContext.get();
    if (requestContext != null) {
      processEnv.setContext(requestContext.toMap());
    }
    processEnv.setSession(new HashMap<>());
    processEnv.setProcessServices(processServices);
    return processEnv;
  }
}
