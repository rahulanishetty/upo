/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.services;

import java.util.Collection;
import java.util.Map;

import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.models.ProcessVariable;

/**
 * Store interface for persisting and retrieving process variables. Manages the storage of variables
 * produced and consumed during process execution, supporting different variable types (INPUT,
 * OUTPUT, ERROR, STATE).
 */
public interface VariableStore {

  /**
   * Saves a variable in the store with its process instance context. The combination of variable
   * (taskId + type) and process instance creates a unique context for the variable.
   *
   * @param variable the variable to save
   * @return true if save was successful, false otherwise
   */
  boolean save(ProcessVariable variable);

  /**
   * Saves a variable in the store with its process instance context. The combination of variable
   * (taskId + type) and process instance creates a unique context for the variable.
   *
   * @param variables the variables to be saved
   * @return true if save was successful, false otherwise
   */
  boolean saveMany(Collection<ProcessVariable> variables);

  /**
   * Retrieves multiple variables by their identifiers. Each identifier should uniquely identify a
   * variable within its process instance context.
   *
   * @param ids collection of variable identifiers to retrieve
   * @return map of id vs variables, empty map if none found
   */
  Map<String, ProcessVariable> findByIds(Collection<String> ids);

  /**
   * Retrieves all variables associated with a process instance. Variables represent the data state
   * and include inputs, outputs, and intermediate values created during process execution.
   *
   * @param processInstance The process instance to retrieve variables for. This can be either a
   *     parent process or a child process instance.
   * @return Collection<Variable> All variables associated with this process instance. Returns empty
   *     collection if no variables exist.
   *     <p>Note: Variables are scoped to the process instance - parent instances cannot directly
   *     access child instance variables and vice versa. Use explicit variable passing for
   *     cross-instance communication.
   */
  Collection<Variable> findVariablesForInstance(ProcessInstance processInstance);

  /**
   * Deletes all variables associated with a process instance. This is typically called during
   * process cleanup after completion or termination.
   *
   * @param processInstanceId The ID of the process instance whose variables should be deleted
   *     <p>Note: This operation is irreversible. Make sure the process instance is in a terminal
   *     state before removing its variables.
   */
  void deleteProcessVariables(String processInstanceId);
}
