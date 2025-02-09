/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.services;

/**
 * Central service registry and locator for process execution services. Provides access to both
 * fixed utility services and runtime-specific service implementations.
 */
public interface ProcessServices {

  /**
   * Retrieves a service implementation by its interface type. Services fall into two categories: 1.
   * Fixed Services - Utility services with immutable implementations (e.g. InputValueResolver) 2.
   * Runtime Services - Services with implementations that vary based on execution strategy (e.g.
   * ProcessInstanceStore for local vs distributed execution)
   *
   * @param <T> The service interface type
   * @param clz The class object representing the service interface
   * @return The service implementation
   */
  <T> T getService(Class<T> clz);
}
