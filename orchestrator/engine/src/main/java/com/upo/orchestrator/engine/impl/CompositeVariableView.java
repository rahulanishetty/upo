/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.ArrayList;
import java.util.List;

import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.VariableContainer;
import com.upo.orchestrator.engine.models.ProcessVariable;
import com.upo.utilities.json.path.JsonPath;

/**
 * Provides a read-only view over multiple VariableContainer instances. Used for variable lookup
 * during evaluation and resolution, where write operations are not needed. Searches through
 * multiple Variables instances in order until a matching variable is found.
 *
 * <p>This class implements the Variables interface but only supports read operations. All write
 * operations throw UnsupportedOperationException.
 */
public class CompositeVariableView implements VariableContainer {

  /**
   * List of Variables instances to search through. Searched in order of addition until a matching
   * variable is found.
   */
  private final List<VariableContainer> containers;

  /** Creates a new empty composite view. */
  public CompositeVariableView() {
    this.containers = new ArrayList<>();
  }

  /**
   * Adds a VariableContainer instance to be included in the search. Variables are searched in the
   * order they are added.
   *
   * @param variableContainer Variables instance to include in search
   */
  public void addVariables(VariableContainer variableContainer) {
    this.containers.add(variableContainer);
  }

  @Override
  public void addNewVariable(String taskId, Variable.Type type, Object payload) {
    throw new UnsupportedOperationException("not supported!");
  }

  @Override
  public void restoreVariable(String taskId, Variable.Type type, Object payload) {
    throw new UnsupportedOperationException("not supported!");
  }

  @Override
  public List<ProcessVariable> getNewVariables() {
    throw new UnsupportedOperationException("not supported!");
  }

  @Override
  public void clearNewVariables() {
    throw new UnsupportedOperationException("not supported!");
  }

  /**
   * Searches for a variable across all registered Variables instances. Returns the first matching
   * variable found, searching Variables in the order they were added.
   *
   * @param taskId identifier of the task that owns the variable
   * @param type type of variable to find
   * @return variable payload if found, null otherwise
   */
  @Override
  public Object getVariable(String taskId, Variable.Type type) {
    for (VariableContainer container : containers) {
      Object payload = container.getVariable(taskId, type);
      if (payload != null) {
        return payload;
      }
    }
    return null;
  }

  @Override
  public boolean containsVariable(String taskId, Variable.Type type) {
    for (VariableContainer container : containers) {
      if (container.containsVariable(taskId, type)) {
        return true;
      }
    }
    return false;
  }

  /**
   * reads for a path across all registered Variables instances. Returns the first matching value
   * found, searching Variables in the order they were added.
   *
   * @param jsonPath processed json path
   * @return resolved payload if found, null otherwise
   */
  @Override
  public Object readVariable(JsonPath jsonPath) {
    for (VariableContainer container : containers) {
      Object value = container.readVariable(jsonPath);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  @Override
  public void closeTransientVariables() {
    for (VariableContainer container : containers) {
      container.closeTransientVariables();
    }
  }
}
