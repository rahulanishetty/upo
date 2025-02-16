/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.services;

import java.util.Map;
import java.util.Optional;

/**
 * Provides access to environment configuration for process execution. Responsible for resolving
 * environment variables needed during process execution setup and runtime.
 */
public interface EnvironmentProvider {

  String DEFAULT_TIER = "DEFAULT";

  /**
   * Returns a map of environment variables required for process execution. These variables
   * typically include: - Configuration settings - System properties - Runtime constants -
   * Environment-specific values
   *
   * @return map of environment variable names to their values
   */
  Map<String, Object> lookupEnvVariables();

  boolean isShutdownInProgress();

  static String getCurrentTier() {
    return Optional.ofNullable(System.getenv("TIER"))
        .filter(s -> !s.isEmpty())
        .orElse(DEFAULT_TIER);
  }
}
