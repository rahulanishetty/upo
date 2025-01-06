/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.api.domain.resiliency;

import java.time.Duration;

/** Defines caching behavior for task execution results. */
public interface CacheConfig {

  /** Returns the time-to-live for cached results. */
  Duration getTtl();

  /** Returns the maximum size of the cache. */
  long getMaxSize();

  /**
   * Returns the cache key generator expression. This expression determines how cache keys are
   * generated from task inputs.
   */
  String getCacheKeyExpression();

  /** Returns whether to cache errors/exceptions. */
  boolean shouldCacheErrors();
}
