/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

/**
 * Provides access to the underlying storage mechanism for specialized operations. This interface
 * should be used with caution as it bypasses the standard repository abstractions.
 *
 * <p>Common use cases: - Batch operations not supported by standard repository interface - Custom
 * Redis operations requiring direct access - Migration or maintenance tasks
 *
 * @implNote Implementations should consider: - Maintaining data consistency with standard
 *     repository operations - Proper key namespace handling
 */
public interface RawStorageAccessor {
  /**
   * Returns the underlying Redis template for direct storage operations. Use with caution as this
   * bypasses standard repository validations and abstractions.
   *
   * @return The Redis template instance used by this repository
   */
  RedisTemplate getRawTemplate();
}
