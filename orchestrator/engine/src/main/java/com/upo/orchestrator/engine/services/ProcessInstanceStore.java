/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.services;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.upo.orchestrator.engine.ProcessFlowStatus;
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
   * Saves or updates a process instances in the store.
   *
   * @param processInstances the process instances to save
   * @return true if save was successful, false otherwise
   */
  boolean saveMany(Collection<ProcessInstance> processInstances);

  /**
   * Saves or updates a process instance in the store if in expected status
   *
   * @param processInstance the process instance to save
   * @return true if save was successful, false otherwise
   */
  boolean save(ProcessInstance processInstance, ProcessFlowStatus expectedStatus);

  /**
   * Retrieves a process instance by its identifier.
   *
   * @param id unique identifier of the process instance
   * @return the process instance if found
   */
  Optional<ProcessInstance> findById(String id);

  /**
   * Retrieves a process instance by its identifier if in expected status.
   *
   * @param id unique identifier of the process instance
   * @param expectedStatus expectedStatus of the process instance
   * @return the process instance if found, null otherwise
   */
  Optional<ProcessInstance> findById(String id, ProcessFlowStatus expectedStatus);

  /**
   * Adds process instance IDs to the existing set if any of instances that the parent process is
   * waiting on. This is typically used in scenarios where a parent process needs to track and wait
   * for multiple child processes to complete.
   *
   * @param parentInstance The parent process instance that will wait for the child instances. This
   *     instance maintains the set of child processes it depends on.
   * @param waitOnInstanceIds Collection of process instance IDs to add to the waiting set. These
   *     IDs represent child process instances that the parent process
   */
  void addWaitingOnInstanceIds(
      ProcessInstance parentInstance, Collection<String> waitOnInstanceIds);

  /**
   * Removes a completed process instance ID from the parent's waiting set and indicates if all
   * child processes have completed. This is called when a child process completes execution and the
   * parent no longer needs to wait for it.
   *
   * @param parentInstance The parent process instance that is waiting on child instances. This
   *     instance maintains the set of child processes it depends on.
   * @param completedInstanceId The ID of the child process instance that has completed execution.
   * @return boolean True if this instance ID was removed from the waiting set, false if it was
   *     already removed (preventing duplicate processing)
   */
  boolean removeCompletedInstanceId(ProcessInstance parentInstance, String completedInstanceId);

  /**
   * Returns the set of child process IDs that the parent is still waiting on. This provides the
   * current set of incomplete child processes without modifying the waiting set.
   *
   * @param parentInstance The parent process instance that is waiting on child instances. This
   *     instance maintains the set of child processes it depends on.
   * @return Set<String> Set of process instance IDs that haven't completed yet. Empty set if all
   *     children are complete.
   *     <p>Note: The result is a point-in-time snapshot and may change if called again, especially
   *     in concurrent scenarios.
   */
  Set<String> getRemainingChildren(ProcessInstance parentInstance);
}
