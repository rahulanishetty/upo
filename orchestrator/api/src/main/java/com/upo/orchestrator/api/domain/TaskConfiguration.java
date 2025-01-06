/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.api.domain;

import java.util.Map;

import com.upo.orchestrator.api.domain.resiliency.ResiliencyConfig;

/**
 * Base configuration for task execution. Separates core task configuration from resiliency
 * configuration to allow resiliency patterns to be applied as decorators to any task type.
 */
public interface TaskConfiguration {

  /**
   * Base configuration for task execution. Separates core task configuration from resiliency
   * configuration to allow resiliency patterns to be applied as decorators to any task type.
   */
  Map<String, Object> getConfiguration();

  /**
   * Returns the resiliency configuration for this task. Defines fault tolerance and reliability
   * patterns like caching, retries, and circuit breaking that can be applied to any task.
   *
   * @return resiliency configuration for the task
   */
  ResiliencyConfig getResiliencyConfig();
}
