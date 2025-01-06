/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.services;

import com.upo.orchestrator.engine.ProcessInstance;

/**
 * Store interface for persisting and retrieving process instances. Provides operations to manage
 * process instance state in a persistent store, supporting both synchronous and distributed
 * execution patterns.
 */
public interface ProcessInstanceStore {

  /**
   * Saves or updates a process instance in the store.
   *
   * @param processInstance the process instance to save
   * @return true if save was successful, false otherwise
   */
  boolean save(ProcessInstance processInstance);

  /**
   * Retrieves a process instance by its identifier. Returns the complete process instance with its
   * current state including environment and variables.
   *
   * @param id unique identifier of the process instance
   * @return the process instance if found, null otherwise
   */
  ProcessInstance findById(String id);
}
