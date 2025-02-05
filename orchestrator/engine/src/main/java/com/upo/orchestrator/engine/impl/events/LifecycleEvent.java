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

@JSONType(
    seeAlso = {
      LifecycleEvent.StartProcess.class,
      LifecycleEvent.ContinueProcessFromTask.class,
      LifecycleEvent.StartProcessInstance.class,
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
  }

  @JSONType(typeName = "start_process_instance")
  public static class StartProcessInstance extends LifecycleEvent {
    private String instanceId;
    private Object payload;

    public StartProcessInstance() {
      super("start_process_instance");
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
  }

  @JSONType(typeName = "continue_process_from_task")
  public static class ContinueProcessFromTask extends LifecycleEvent {
    private String processInstanceId;
    private String taskId;

    public ContinueProcessFromTask() {
      super("continue_process_from_task");
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
  }

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
  }
}
