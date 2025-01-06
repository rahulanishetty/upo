/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.services;

import java.util.Collection;
import java.util.List;

import com.upo.orchestrator.engine.ProcessInstance;
import com.upo.orchestrator.engine.Variable;

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
   * @param processInstance process instance context for the variable
   * @return true if save was successful, false otherwise
   */
  boolean save(Variable variable, ProcessInstance processInstance);

  /**
   * Retrieves multiple variables by their identifiers. Each identifier should uniquely identify a
   * variable within its process instance context.
   *
   * @param ids collection of variable identifiers to retrieve
   * @return list of matched variables, empty list if none found
   */
  List<Variable> findVariables(Collection<String> ids);
}
