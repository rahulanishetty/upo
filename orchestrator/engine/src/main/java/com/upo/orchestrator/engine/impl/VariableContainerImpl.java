/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.*;
import java.util.concurrent.Future;

import com.upo.orchestrator.engine.TaskExecutionException;
import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.VariableContainer;
import com.upo.orchestrator.engine.models.ProcessEnv;
import com.upo.orchestrator.engine.models.ProcessVariable;
import com.upo.utilities.ds.Pair;
import com.upo.utilities.json.path.JsonPath;

public class VariableContainerImpl implements VariableContainer {

  private static final Set<String> RESERVED_KEYS = Set.of("env", "ctx", "session");

  private final Map<String, Map<String, Object>> variables;
  private final Map<Pair<String, Variable.Type>, Object> newVariables;

  public VariableContainerImpl() {
    this.variables = new HashMap<>();
    this.newVariables = new HashMap<>();
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
    payload = restoreVariableInternal(taskId, type, payload);
    newVariables.put(Pair.of(taskId, type), payload);
  }

  @Override
  public void restoreVariable(String taskId, Variable.Type type, Object payload) {
    restoreVariableInternal(taskId, type, payload);
  }

  @Override
  public List<ProcessVariable> getNewVariables() {
    List<ProcessVariable> toResolve = findVariablesToResolve();
    ensureResolved(toResolve);
    return newVariables.entrySet().stream()
        .map(
            entry -> {
              ProcessVariable variable = new ProcessVariable();
              variable.setTaskId(entry.getKey().getFirstElement());
              variable.setType(entry.getKey().getSecondElement());
              variable.setPayload(entry.getValue());
              return variable;
            })
        .toList();
  }

  @Override
  public void clearNewVariables() {
    newVariables.clear();
  }

  @Override
  public Object getVariable(String taskId, Variable.Type type) {
    Object payload = getVariableInternal(taskId, type);
    return ensureResolved(taskId, type, payload);
  }

  @Override
  public Object readVariable(JsonPath jsonPath) {
    String taskId = jsonPath.getToken(0);
    if (!RESERVED_KEYS.contains(taskId)) {
      String type = jsonPath.getToken(1);
      Variable.Type varType = Variable.Type.fromKey(type);
      Object payload = getVariableInternal(taskId, varType);
      if (ensureResolved(taskId, varType, payload) == null) {
        return null;
      }
    }
    return jsonPath.read(variables);
  }

  private Object restoreVariableInternal(String taskId, Variable.Type type, Object payload) {
    Map<String, Object> typeAndPayload = variables.computeIfAbsent(taskId, t -> new HashMap<>());
    if (payload instanceof Future<?> future) {
      if (future.isDone()) {
        payload = waitForVariable(taskId, type, future);
      }
    }
    typeAndPayload.put(type.getKey(), payload);
    return payload;
  }

  private void ensureResolved(List<ProcessVariable> toResolve) {
    if (toResolve == null || toResolve.isEmpty()) {
      return;
    }
    for (ProcessVariable variable : toResolve) {
      ensureResolved(variable.getTaskId(), variable.getType(), variable.getPayload());
    }
  }

  private List<ProcessVariable> findVariablesToResolve() {
    List<ProcessVariable> toResolve = new ArrayList<>();
    for (Map.Entry<Pair<String, Variable.Type>, Object> entry : newVariables.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof Future<?>) {
        ProcessVariable variable = new ProcessVariable();
        variable.setTaskId(entry.getKey().getFirstElement());
        variable.setType(entry.getKey().getSecondElement());
        variable.setPayload(entry.getValue());
        toResolve.add(variable);
      }
    }
    return toResolve;
  }

  private Object getVariableInternal(String taskId, Variable.Type type) {
    Map<String, Object> payloadByType = variables.get(taskId);
    if (payloadByType == null) {
      return null;
    }
    return payloadByType.get(type.getKey());
  }

  private Object ensureResolved(String taskId, Variable.Type varType, Object payload) {
    if (payload instanceof Future<?> future) {
      Variable.Type resolvedType = varType;
     // wait for completion
      Object resolvedPayload = waitForVariable(taskId, varType, future);
      if (resolvedPayload instanceof Pair<?, ?> pair) {
        resolvedType = (Variable.Type) pair.getFirstElement();
        resolvedPayload = pair.getSecondElement();
      }
      addNewVariable(taskId, resolvedType, resolvedPayload);
      if (!Objects.equals(resolvedType, varType)) {
        removeNewVariable(taskId, varType);
        return null;
      }
      return resolvedPayload;
    } else {
      return payload;
    }
  }

  private void removeNewVariable(String taskId, Variable.Type type) {
    Map<String, Object> payloadByType = variables.get(taskId);
    if (payloadByType != null) {
      payloadByType.remove(type.getKey());
    }
    newVariables.remove(Pair.of(taskId, type));
  }

  private Object waitForVariable(String taskId, Variable.Type type, Future<?> future) {
    boolean interrupted = false;
    try {
      return future.get();
    } catch (Exception eX) {
      if (eX instanceof InterruptedException) {
        interrupted = true;
      }
      throw new TaskExecutionException(
          "failed while waiting for variable of task: " + taskId + ", type: " + type, eX);
    } finally {
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
