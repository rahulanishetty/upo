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
}
