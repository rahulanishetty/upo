/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.*;

import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.VariableContainer;
import com.upo.orchestrator.engine.models.ProcessEnv;
import com.upo.orchestrator.engine.models.ProcessVariable;
import com.upo.utilities.json.path.JsonPath;

public class VariableContainerImpl implements VariableContainer {

  private final Map<String, Map<String, Object>> variables;
  private final List<ProcessVariable> newVariables;

  public VariableContainerImpl() {
    this.variables = new HashMap<>();
    this.newVariables = new ArrayList<>();
  }

  /**
   * Adds process environment variables to make them accessible via JsonPath expressions. Maps
   * environment components to specific identifiers: - 'env': Environment variables ($.env.*) -
   * 'ctx': Context variables ($.ctx.*) - 'session': Session variables ($.session.*)
   *
   * <p>Example JsonPath access: - env.region - ctx.tenantId - session.userId
   *
   * <p>Only non-null components from ProcessEnv are added. Each component is added with its
   * respective identifier for JsonPath resolution.
   *
   * @param processEnv process environment containing variables to add
   */
  public void addProcessEnvVariables(ProcessEnv processEnv) {
    if (processEnv.getEnv() != null) {
      this.variables.put("env", processEnv.getEnv());
    }
    if (processEnv.getContext() != null) {
      this.variables.put("ctx", processEnv.getEnv());
    }
    if (processEnv.getSession() != null) {
      this.variables.put("session", processEnv.getEnv());
    }
  }

  @Override
  public void addNewVariable(String taskId, Variable.Type type, Object payload) {
    restoreVariable(taskId, type, payload);
    addOrUpdateNewVariables(taskId, type, payload);
  }

  @Override
  public void restoreVariable(String taskId, Variable.Type type, Object payload) {
    Map<String, Object> typeAndPayload = variables.computeIfAbsent(taskId, t -> new HashMap<>());
    typeAndPayload.put(type.name(), payload);
  }

  @Override
  public List<ProcessVariable> getNewVariables() {
    return newVariables;
  }

  @Override
  public Object getVariable(String taskId, Variable.Type type) {
    Map<String, Object> payloadByType = variables.get(taskId);
    if (payloadByType == null) {
      return null;
    }
    return payloadByType.get(type.name());
  }

  @Override
  public Object readVariable(JsonPath jsonPath) {
    return jsonPath.read(variables);
  }

  private void addOrUpdateNewVariables(String taskId, Variable.Type type, Object payload) {
    boolean found = false;
    for (ProcessVariable variable : newVariables) {
      if (Objects.equals(variable.getTaskId(), taskId)
          && Objects.equals(variable.getType(), type)) {
        variable.setPayload(payload);
        found = true;
        break;
      }
    }
    if (!found) {
      ProcessVariable variable = new ProcessVariable();
      variable.setTaskId(taskId);
      variable.setType(type);
      variable.setPayload(payload);
      newVariables.add(variable);
    }
  }
}
