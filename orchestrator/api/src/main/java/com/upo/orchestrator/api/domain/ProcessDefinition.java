/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.api.domain;

import java.util.Map;
import java.util.Optional;

import com.upo.utilities.filter.api.Filter;

/**
 * Represents a complete process definition in the orchestration system. A process is a directed
 * graph of tasks connected by transitions, defining a workflow that can be executed by the
 * orchestrator.
 */
public interface ProcessDefinition {

  /**
   * Returns the unique identifier of the process. This ID should be unique across the system and
   * can be used to reference this process.
   *
   * @return the process identifier
   */
  String getId();

  /**
   * Returns the version of the process definition. Versions allow multiple definitions of the same
   * process to exist, enabling process evolution while maintaining backward compatibility.
   *
   * @return the version identifier
   */
  String getVersion();

  /**
   * Returns the ID of the first task that should be executed when the process starts. This task
   * serves as the entry point for the process execution.
   *
   * @return the ID of the starting task
   */
  String getStartTaskId();

  /**
   * Returns all task definitions that make up this process. The map keys are task IDs, and values
   * are the corresponding task definitions. This collection represents all possible tasks that can
   * be executed as part of this process.
   *
   * @return map of task IDs to their definitions
   */
  Map<String, TaskDefinition> getTaskDefinitions();

  /**
   * Returns the predicate that determines whether this process should be executed. The process
   * execution follows these rules: - If predicate is null, process executes normally - If predicate
   * evaluates to true, process executes normally - If predicate evaluates to false, process is
   * skipped
   *
   * <p>This can be used to control process execution based on: - Configuration settings - Runtime
   * conditions - System state - External triggers - Business rules
   *
   * <p>Example uses: - Skip process if feature flag is disabled - Execute process only in certain
   * environments - Run process based on schedule or time window - Control process based on system
   * load or resources - Execute process based on business conditions
   *
   * @return the process execution predicate, null if process should always execute
   */
  Optional<Filter> getPredicate();
}
