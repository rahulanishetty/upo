/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.models;

import com.alibaba.fastjson2.annotation.JSONField;
import com.upo.orchestrator.engine.ProcessDetails;
import com.upo.orchestrator.engine.ProcessFlowStatus;
import com.upo.orchestrator.engine.ProcessOutcomeSink;
import com.upo.orchestrator.engine.VariableContainer;
import com.upo.orchestrator.engine.impl.VariableContainerImpl;
import com.upo.utilities.json.Utils;

/**
 * Represents a single execution of a process. Maintains the state and context of process execution
 * from start to completion. Can be a root process instance or a child of another process instance.
 */
public class ProcessInstance {

  public static final String STATUS = "status";

  /** Unique identifier for this process instance. */
  private String id;

  /** ID of the root process instance. */
  private String rootId;

  /** ID of the parent process instance. Null for root processes, set for child processes. */
  private String parentId;

  /** ID of the process definition being executed. */
  private String processId;

  /** ID of the process snapshot executing this instance. */
  private String processSnapshotId;

  /** version of the process executing this instance. */
  private String processVersion;

  /** execution strategy used for this instance */
  private String executionStrategy;

  /** represents if this process instance is a concurrent instance */
  private boolean concurrent;

  /**
   * Optional task ID where process execution should terminate. Used to execute subset of process or
   * handle early termination.
   */
  private String terminateAtTaskId;

  /** Timestamp when process instance started. */
  private Long startTime;

  /** Timestamp when process instance completed. Null if process is still executing. */
  private Long endTime;

  /** Status of current task */
  private ProcessFlowStatus status;

  /** ID of the currently executing task. */
  private String currTaskId;

  /** Number of tasks executed in total */
  private Long taskCount = 0L;

  /** Number of tasks executed since last instance save */
  @JSONField(serialize = false, deserialize = false)
  private Long taskCountSinceLastFlush = 0L;

  /** Timestamp when current task started execution. Reset each time a new task begins. */
  private Long currentTaskStartTime;

  /** Timestamp of last signal received by current task. Reset when moving to next task. */
  private Long currentTaskSignalTime;

  /** Timestamp when current task completed execution. Reset when moving to next task. */
  private Long currentTaskEndTime;

  /** Timestamp when current task invoked execution. Reset each time a new task begins. */
  private Long currentTaskInvocationTime;

  /** ID of the previously executed task. */
  private String prevTaskId;

  /** Environment containing execution context and services. */
  private ProcessEnv processEnv;

  /** Variables produced and consumed during process execution. */
  @JSONField(serialize = false, deserialize = false)
  private VariableContainer variableContainer;

  /** input passed for this instance */
  @JSONField(serialize = false, deserialize = false)
  private Object input;

  /** outcome sink for this instance */
  @JSONField(serialize = false, deserialize = false)
  private ProcessOutcomeSink sink;

  public ProcessInstance() {}

  public ProcessInstance(ProcessInstance processInstance) {
    this.id = processInstance.id;
    this.rootId = processInstance.rootId;
    this.parentId = processInstance.parentId;
    this.processId = processInstance.processId;
    this.processSnapshotId = processInstance.processSnapshotId;
    this.processVersion = processInstance.processVersion;
    this.executionStrategy = processInstance.executionStrategy;
    this.concurrent = processInstance.concurrent;
    this.terminateAtTaskId = processInstance.terminateAtTaskId;
    this.startTime = processInstance.startTime;
    this.endTime = processInstance.endTime;
    this.status = processInstance.status;
    this.currTaskId = processInstance.currTaskId;
    this.taskCount = processInstance.taskCount;
    this.taskCountSinceLastFlush = 0L;
    this.currentTaskStartTime = processInstance.currentTaskStartTime;
    this.currentTaskSignalTime = processInstance.currentTaskSignalTime;
    this.currentTaskEndTime = processInstance.currentTaskEndTime;
    this.currentTaskInvocationTime = processInstance.currentTaskInvocationTime;
    this.prevTaskId = processInstance.prevTaskId;
    this.processEnv = processInstance.processEnv.copy();
    VariableContainerImpl container = new VariableContainerImpl();
    container.addProcessEnvVariables(this.processEnv);
    this.variableContainer = container;
    this.input = processInstance.input;
    if (processInstance.sink != null) {
      if (processInstance.sink.isSerializable()) {
        this.sink = Utils.deepCopyViaJson(processInstance.sink, ProcessOutcomeSink.class);
      } else {
        this.sink = processInstance.sink;
      }
    } else {
      this.sink = null;
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getRootId() {
    return rootId;
  }

  public void setRootId(String rootId) {
    this.rootId = rootId;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getProcessId() {
    return processId;
  }

  public void setProcessId(String processId) {
    this.processId = processId;
  }

  public String getProcessSnapshotId() {
    return processSnapshotId;
  }

  public void setProcessSnapshotId(String processRuntimeId) {
    this.processSnapshotId = processRuntimeId;
  }

  public String getProcessVersion() {
    return processVersion;
  }

  public void setProcessVersion(String processVersion) {
    this.processVersion = processVersion;
  }

  public String getExecutionStrategy() {
    return executionStrategy;
  }

  public void setExecutionStrategy(String executionStrategy) {
    this.executionStrategy = executionStrategy;
  }

  public boolean isConcurrent() {
    return concurrent;
  }

  public void setConcurrent(boolean concurrent) {
    this.concurrent = concurrent;
  }

  public String getTerminateAtTaskId() {
    return terminateAtTaskId;
  }

  public void setTerminateAtTaskId(String terminateAtTaskId) {
    this.terminateAtTaskId = terminateAtTaskId;
  }

  public Long getStartTime() {
    return startTime;
  }

  public void setStartTime(Long startTime) {
    this.startTime = startTime;
  }

  public Long getEndTime() {
    return endTime;
  }

  public void setEndTime(Long endTime) {
    this.endTime = endTime;
  }

  public ProcessFlowStatus getStatus() {
    return status;
  }

  public void setStatus(ProcessFlowStatus status) {
    this.status = status;
  }

  public String getCurrTaskId() {
    return currTaskId;
  }

  public void setCurrTaskId(String currTaskId) {
    this.currTaskId = currTaskId;
  }

  public Long getCurrentTaskStartTime() {
    return currentTaskStartTime;
  }

  public void setCurrentTaskStartTime(Long currentTaskStartTime) {
    this.currentTaskStartTime = currentTaskStartTime;
  }

  public Long getCurrentTaskSignalTime() {
    return currentTaskSignalTime;
  }

  public void setCurrentTaskSignalTime(Long currentTaskSignalTime) {
    this.currentTaskSignalTime = currentTaskSignalTime;
  }

  public Long getCurrentTaskEndTime() {
    return currentTaskEndTime;
  }

  public void setCurrentTaskEndTime(Long currentTaskEndTime) {
    this.currentTaskEndTime = currentTaskEndTime;
  }

  public Long getCurrentTaskInvocationTime() {
    return currentTaskInvocationTime;
  }

  public void setCurrentTaskInvocationTime(Long currentTaskInvocationTime) {
    this.currentTaskInvocationTime = currentTaskInvocationTime;
  }

  public String getPrevTaskId() {
    return prevTaskId;
  }

  public void setPrevTaskId(String prevTaskId) {
    this.prevTaskId = prevTaskId;
  }

  public ProcessEnv getProcessEnv() {
    return processEnv;
  }

  public void setProcessEnv(ProcessEnv processEnv) {
    this.processEnv = processEnv;
  }

  public VariableContainer getVariableContainer() {
    return variableContainer;
  }

  public void setVariableContainer(VariableContainer variableContainer) {
    this.variableContainer = variableContainer;
  }

  public Long getTaskCount() {
    return taskCount;
  }

  public void setTaskCount(Long taskCount) {
    this.taskCount = taskCount;
  }

  public Long getTaskCountSinceLastFlush() {
    return taskCountSinceLastFlush;
  }

  public void setTaskCountSinceLastFlush(Long taskCountSinceLastFlush) {
    this.taskCountSinceLastFlush = taskCountSinceLastFlush;
  }

  public long incrementTaskCount() {
    taskCount++;
    return taskCountSinceLastFlush++;
  }

  public Object getInput() {
    return input;
  }

  public void setInput(Object input) {
    this.input = input;
  }

  public ProcessOutcomeSink getSink() {
    return sink;
  }

  public void setSink(ProcessOutcomeSink sink) {
    this.sink = sink;
  }

  public ProcessDetails toProcessDetails() {
    return new ProcessDetails() {
      @Override
      public String getId() {
        return ProcessInstance.this.getProcessId();
      }

      @Override
      public String getSnapshotId() {
        return ProcessInstance.this.getProcessSnapshotId();
      }

      @Override
      public String getSnapshotVersion() {
        return ProcessInstance.this.getProcessVersion();
      }
    };
  }

  @Override
  public String toString() {
    return "ProcessInstance{"
        + "id='"
        + id
        + '\''
        + ", rootId='"
        + rootId
        + '\''
        + ", parentId='"
        + parentId
        + '\''
        + ", processId='"
        + processId
        + '\''
        + ", processSnapshotId='"
        + processSnapshotId
        + '\''
        + ", processVersion='"
        + processVersion
        + '\''
        + '}';
  }
}
