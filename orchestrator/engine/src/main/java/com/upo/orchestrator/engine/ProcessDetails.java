/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

/**
 * Contains identifying information for a process and its deployed version. Links a process
 * definition to its specific released/deployed version that will be executed.
 */
public interface ProcessDetails {

  /**
   * Returns the unique identifier of the process. This is the base identifier that remains constant
   * across all versions of the process.
   *
   * @return process identifier
   */
  String getId();

  /**
   * Returns the release identifier of this process. Represents a specific deployed version of the
   * process that has been validated and approved for execution.
   *
   * @return release identifier
   */
  String getSnapshotId();

  /**
   * Returns the version number of this release. Provides version tracking for process releases,
   * allowing for version control and rollback.
   *
   * @return version number of the release
   */
  String getSnapshotVersion();
}
