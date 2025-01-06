/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.api.domain.resiliency;

import java.time.Duration;
import java.util.Optional;

/** Combines all resiliency options for a task. */
public interface ResiliencyConfig {
  /** Returns the caching configuration. */
  Optional<CacheConfig> getCacheConfig();

  /** Returns the circuit breaker configuration. */
  Optional<CircuitBreakerConfig> getCircuitBreakerConfig();

  /** Returns the retry configuration. */
  Optional<RetryConfig> getRetryConfig();

  /** Returns the timeout for task execution. */
  Optional<Duration> getTimeout();
}
