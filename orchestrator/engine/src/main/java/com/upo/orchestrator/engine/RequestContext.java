/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import java.util.Map;

/**
 * Represents the context of a current request during process execution. Provides access to
 * request-specific data that needs to be available throughout the execution lifecycle.
 */
public interface RequestContext {

  /**
   * Converts the request context to a map representation. This map typically includes: - Request
   * headers - Security context - Correlation IDs - Tenant information - Request metadata - User
   * context
   *
   * @return map containing all request context data
   */
  Map<String, Object> toMap();

  /**
   * Returns the current request context. This is typically bound to the current thread or execution
   * scope.
   *
   * @return current request context
   */
  static RequestContext get() {
    throw new UnsupportedOperationException("TODO Implement this!");
  }
}
