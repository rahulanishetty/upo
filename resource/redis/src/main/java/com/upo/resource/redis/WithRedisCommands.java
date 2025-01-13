/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

/**
 * Provider interface for accessing Redis commands. Implementors guarantee proper handling of Redis
 * connections and commands access.
 *
 * <p>This interface is typically used as part of Redis operations management where direct access to
 * Redis commands is needed. Implementations should ensure: - Resource cleanup
 *
 * @see RedisCommands
 */
public interface WithRedisCommands {

  /**
   * Returns a RedisCommands instance for executing Redis operations. The instance should be
   * properly initialized and ready for use.
   *
   * <p>Note: Callers should ensure proper resource cleanup when using the returned RedisCommands
   * instance, typically through try-with-resources.
   *
   * @return An instance of RedisCommands ready for executing Redis operations
   */
  RedisCommands getRedisCommands();
}
