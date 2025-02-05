/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.events;

import com.alibaba.fastjson2.annotation.JSONType;
import com.upo.orchestrator.engine.Signal;

/**
 * Base class for process lifecycle events that represent different execution control operations.
 * Each event type corresponds to a specific process control action and includes a partition key for
 * distributed processing.
 */
@JSONType(
    seeAlso = {
      LifecycleEvent.StartProcess.class,
      LifecycleEvent.ExecuteFromTask.class,
      LifecycleEvent.StartExistingInstance.class,
      LifecycleEvent.SignalProcess.class,
    },
    typeKey = "type",
    orders = {"type"})
public abstract class LifecycleEvent {

  private final String type;

  public LifecycleEvent(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  /**
   * Returns the partition key for distributed processing of this event. Different event types use
   * different partition keys to ensure related operations are processed by the same partition.
   *
   * @return String partition key for this event
   */
  public abstract String getPartitionKey();

  /** Event for starting a new process execution with a process definition. */
  @JSONType(typeName = "start_process")
  public static class StartProcess extends LifecycleEvent {
    private String processDefinitionId;
    private Object payload;

    public StartProcess() {
      super("start_process");
    }

    public String getProcessDefinitionId() {
      return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
      this.processDefinitionId = processDefinitionId;
    }

    public Object getPayload() {
      return payload;
    }

    public void setPayload(Object payload) {
      this.payload = payload;
    }

    @Override
    public String getPartitionKey() {
      return processDefinitionId;
    }
  }

  /**
   * Event for starting a new process instance with a specific ID. Unlike StartProcess, this allows
   * to start process with a pre-created instance
   */
  @JSONType(typeName = "start_instance")
  public static class StartExistingInstance extends LifecycleEvent {
    private String instanceId;
    private Object payload;

    public StartExistingInstance() {
      super("start_instance");
    }

    public String getInstanceId() {
      return instanceId;
    }

    public void setInstanceId(String instanceId) {
      this.instanceId = instanceId;
    }

    public Object getPayload() {
      return payload;
    }

    public void setPayload(Object payload) {
      this.payload = payload;
    }

    @Override
    public String getPartitionKey() {
      return instanceId;
    }
  }

  /**
   * Event for executing a process from a specific task. Used to initiate execution at a given task
   * rather than from process start.
   */
  @JSONType(typeName = "execute_from_task")
  public static class ExecuteFromTask extends LifecycleEvent {
    private String processInstanceId;
    private String taskId;

    public ExecuteFromTask() {
      super("execute_from_task");
    }

    public String getProcessInstanceId() {
      return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
      this.processInstanceId = processInstanceId;
    }

    public String getTaskId() {
      return taskId;
    }

    public void setTaskId(String taskId) {
      this.taskId = taskId;
    }

    @Override
    public String getPartitionKey() {
      return processInstanceId;
    }
  }

  /**
   * Event for sending a signal to a process instance. Used for inter-process communication and
   * state transitions.
   */
  @JSONType(typeName = "signal_process")
  public static class SignalProcess extends LifecycleEvent {
    private String processInstanceId;
    private Signal signal;

    public SignalProcess() {
      super("signal_process");
    }

    public String getProcessInstanceId() {
      return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
      this.processInstanceId = processInstanceId;
    }

    public Signal getSignal() {
      return signal;
    }

    public void setSignal(Signal signal) {
      this.signal = signal;
    }

    @Override
    public String getPartitionKey() {
      return processInstanceId;
    }
  }
}
