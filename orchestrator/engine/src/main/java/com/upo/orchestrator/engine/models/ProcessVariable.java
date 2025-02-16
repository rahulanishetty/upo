/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.models;

import com.upo.orchestrator.engine.Variable;

public class ProcessVariable implements Variable {
  private String id;
  private String processInstanceId;
  private String taskId;
  private Variable.Type type;
  private Object payload;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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
  public Variable.Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Object getPayload() {
    return payload;
  }

  public void setPayload(Object payload) {
    this.payload = payload;
  }

  public void initId(ProcessInstance processInstance) {
    setId(getId(processInstance, taskId, type));
    setProcessInstanceId(processInstance.getId());
  }

  public static String getId(ProcessInstance processInstance, String taskId, Type type) {
    String rootInstanceId = processInstance.getRootId();
    if (rootInstanceId == null || rootInstanceId.isEmpty()) {
      rootInstanceId = processInstance.getId();
    }
    return rootInstanceId + "/" + processInstance.getId() + "/" + taskId + "/" + type.getKey();
  }
}
