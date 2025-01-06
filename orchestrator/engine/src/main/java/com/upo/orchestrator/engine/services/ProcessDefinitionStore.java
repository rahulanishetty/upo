/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.services;

import com.upo.orchestrator.api.domain.ProcessDefinition;

/**
 * Store interface for persisting and retrieving process definitions. Manages the storage of process
 * definitions which serve as templates for process execution. Each definition includes task
 * configurations, flow logic, and execution rules.
 */
public interface ProcessDefinitionStore {

  /**
   * Saves or updates a process definition in the store. Should handle both new definitions and
   * updates to existing ones. In case of updates, should maintain version history if supported.
   *
   * @param processDefinition the process definition to save
   * @return true if save was successful, false otherwise
   */
  boolean save(ProcessDefinition processDefinition);

  /**
   * Retrieves a process definition by its identifier. Returns the complete definition including all
   * task definitions, transitions, and configuration.
   *
   * @param id unique identifier of the process definition
   * @return the process definition if found, null otherwise
   */
  ProcessDefinition findById(String id);
}
