/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import java.util.List;

import com.upo.orchestrator.engine.models.ProcessVariable;
import com.upo.utilities.json.path.JsonPath;

/**
 * Manages variables during process execution. Variables represent the data produced and consumed by
 * tasks during execution, including inputs, outputs, errors, and state changes.
 */
public interface VariableContainer {

  /**
   * Creates and adds a new variable during task execution. Used when task produces new data such
   * as: - Processed inputs after variable resolution - Task output after execution - State changes
   * during execution - Error information on failure
   *
   * @param taskId identifier of the task producing the variable
   * @param type type of variable being added
   * @param payload variable data
   */
  void addNewVariable(String taskId, Variable.Type type, Object payload);

  /**
   * Restores an existing variable from persistent storage. Used when loading previously stored
   * variables, such as: - Loading process state after resume - Accessing historical task data -
   * Retrieving variables from parent processes
   *
   * @param taskId identifier of the task that owns the variable
   * @param type type of variable to restore
   * @param payload variable data from storage
   */
  void restoreVariable(String taskId, Variable.Type type, Object payload);

  /**
   * Returns all new variables created during current execution. Only includes variables added via
   * addNewVariable, not those restored from storage.
   *
   * @return list of newly created variables
   */
  List<ProcessVariable> getNewVariables();

  /**
   * empties container maintaining all new variables created during current execution. Only retains
   * variables as if they were restored from storage.
   */
  void clearNewVariables();

  /**
   * Retrieves variable data for a specific task and type.
   *
   * @param taskId identifier of the task
   * @param type type of variable to retrieve
   * @return variable data, or null if not found
   */
  Object getVariable(String taskId, Variable.Type type);

  /**
   * Checks if variable data exists for a specific task and type.
   *
   * @param taskId identifier of the task
   * @param type type of variable to retrieve
   * @return if found - true, false otherwise
   */
  boolean containsVariable(String taskId, Variable.Type type);

  /**
   * Retrieves variable data for a specific json path.
   *
   * @param jsonPath processed json path
   * @return variable data, or null if not found
   */
  Object readVariable(JsonPath jsonPath);
}
