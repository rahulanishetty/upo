/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.VariableContainer;
import com.upo.orchestrator.engine.models.ProcessVariable;
import com.upo.utilities.json.path.JsonPath;

/**
 * An immutable implementation of VariableContainer that only allows reading and restoring
 * variables. This implementation prevents modification of variables through the normal add/update
 * operations while supporting variable restoration for internal state management.
 *
 * <p>The container supports two types of variables: 1. Simple variables: stored directly with their
 * taskId 2. Typed variables: stored in a map under taskId with type-specific keys
 *
 * @see VariableContainer
 * @see Variable.Type
 */
public class ImmutableVariableContainer implements VariableContainer {

  private final Map<String, Object> variables = new HashMap<>();

  /**
   * Operation not supported in this implementation.
   *
   * @throws IllegalStateException always, as adding new variables is not supported
   */
  @Override
  public void addNewVariable(String taskId, Variable.Type type, Object payload) {
    throw new IllegalStateException("adding new variable is not supported");
  }

  /**
   * Restores a variable value in the container. This operation is allowed to support internal state
   * management while maintaining immutability from the user perspective.
   *
   * <p>For simple variables (type == null), the payload is stored directly. For typed variables,
   * the payload is stored in a map under the taskId with type-specific key.
   *
   * @param taskId The identifier of the task this variable belongs to
   * @param type The type of the variable, or null for simple variables
   * @param payload The value to restore
   * @throws IllegalStateException if attempting to restore a typed value where a non-map value
   *     exists
   */
  @Override
  public void restoreVariable(String taskId, Variable.Type type, Object payload) {
    if (type == null) {
      variables.put(taskId, payload);
      return;
    }

    Object existingValue = variables.get(taskId);
    if (existingValue instanceof Map<?, ?> map) {
      map = new LinkedHashMap<>(map);
     //noinspection unchecked
      ((Map<String, Object>) map).put(type.getKey(), payload);
      variables.put(taskId, map);
    } else if (existingValue == null) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put(type.getKey(), payload);
      variables.put(taskId, map);
    } else {
      throw new IllegalStateException(
          String.format("Type mismatch for taskId: %s, type: %s", taskId, type.getKey()));
    }
  }

  @Override
  public List<ProcessVariable> getNewVariables() {
    throw new IllegalStateException("adding new variable is not supported");
  }

  @Override
  public void clearNewVariables() {
    throw new IllegalStateException("adding new variable is not supported");
  }

  /**
   * Retrieves a variable value from the container.
   *
   * @param taskId The identifier of the task whose variable to retrieve
   * @param type The type of variable to retrieve, or null for simple variables
   * @return The variable value, or null if not found
   */
  @Override
  public Object getVariable(String taskId, Variable.Type type) {
    Object value = variables.get(taskId);
    if (type != null) {
      if (value instanceof Map<?, ?> map) {
        return map.get(type.getKey());
      }
      return null;
    }
    return value;
  }

  @Override
  public boolean containsVariable(String taskId, Variable.Type type) {
    return getVariable(taskId, type) != null;
  }

  @Override
  public Object readVariable(JsonPath jsonPath) {
    return jsonPath.read(variables);
  }
}
