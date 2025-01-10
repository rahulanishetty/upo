/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.services;

import com.upo.orchestrator.engine.ExecutionResult;
import com.upo.orchestrator.engine.models.ProcessInstance;

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
   * Saves or updates a process instance in the store if in expected status
   *
   * @param processInstance the process instance to save
   * @return true if save was successful, false otherwise
   */
  boolean save(ProcessInstance processInstance, ExecutionResult.Status expectedStatus);

  /**
   * Retrieves a process instance by its identifier.
   *
   * @param id unique identifier of the process instance
   * @return the process instance if found, null otherwise
   */
  ProcessInstance findById(String id);

  /**
   * Retrieves a process instance by its identifier if in expected status.
   *
   * @param id unique identifier of the process instance
   * @param expectedStatus expectedStatus of the process instance
   * @return the process instance if found, null otherwise
   */
  ProcessInstance findById(String id, ExecutionResult.Status expectedStatus);
}
