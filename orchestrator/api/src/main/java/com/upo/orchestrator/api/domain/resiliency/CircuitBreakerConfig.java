/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.api.domain.resiliency;

import java.time.Duration;

/** Defines circuit breaker configuration for fault tolerance. */
public interface CircuitBreakerConfig {
  /** Returns the failure threshold that triggers open circuit. */
  double getFailureThreshold();

  /** Returns the minimum number of calls before calculating failure rate. */
  int getMinimumNumberOfCalls();

  /** Returns the wait duration before attempting to close circuit. */
  Duration getWaitDuration();

  /** Returns permitted number of calls in half-open state. */
  int getPermittedCallsInHalfOpenState();
}
