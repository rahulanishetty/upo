/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.models;

import com.upo.orchestrator.engine.Variables;

/**
 * Represents a single execution of a process. Maintains the state and context of process execution
 * from start to completion. Can be a root process instance or a child of another process instance.
 */
public class ProcessInstance {

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

  /**
   * Optional task ID where process execution should terminate. Used to execute subset of process or
   * handle early termination.
   */
  private String terminateAtTaskId;

  /** Timestamp when process instance started. */
  private Long startTime;

  /** Timestamp when process instance completed. Null if process is still executing. */
  private Long endTime;

  /** ID of the currently executing task. */
  private String currTaskId;

  /** Timestamp when current task started execution. Reset each time a new task begins. */
  private Long currentTaskStartTime;

  /** Timestamp of last signal received by current task. Reset when moving to next task. */
  private Long currentTaskSignalTime;

  /** Timestamp when current task completed execution. Reset when moving to next task. */
  private Long currentTaskEndTime;

  /** ID of the previously executed task. */
  private String prevTaskId;

  /** Environment containing execution context and services. */
  private ProcessEnv processEnv;

  /** Variables produced and consumed during process execution. */
  private Variables variables;

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

  public Variables getVariables() {
    return variables;
  }

  public void setVariables(Variables variables) {
    this.variables = variables;
  }
}
