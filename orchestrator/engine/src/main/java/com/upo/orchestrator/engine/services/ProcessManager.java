/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.services;

import com.upo.orchestrator.engine.ProcessRuntime;

/**
 * Manages process runtimes by providing methods to retrieve or create process runtime instances.
 *
 * <p>This interface facilitates the management of process runtimes, allowing for dynamic creation
 * and retrieval of runtime instances based on unique identifiers.
 */
public interface ProcessManager {

  /**
   * Retrieves an existing process runtime or creates a new one if it does not exist.
   *
   * <p>This method ensures that for a given identifier, a single process runtime instance is
   * maintained. If no runtime exists for the specified ID, a new runtime is created.
   *
   * @param processId A unique identifier for the process definition snapshot
   * @return An existing or newly created ProcessRuntime instance
   */
  ProcessRuntime getOrCreateRuntime(String processId);

  /**
   * Retrieves an existing process runtime or creates a new one if it does not exist.
   *
   * <p>This method ensures that for a given identifier, a single process runtime instance is
   * maintained. If no runtime exists for the specified ID, a new runtime is created.
   *
   * @param processSnapshotId A unique identifier for the process definition snapshot
   * @return An existing or newly created ProcessRuntime instance
   */
  ProcessRuntime getOrCreateRuntimeForSnapshot(String processSnapshotId);
}
