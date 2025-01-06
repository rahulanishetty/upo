/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.api.domain.resiliency;

import java.time.Duration;

import com.upo.utilities.filter.api.Filter;

/**
 * Defines retry behavior for failed task executions. Uses a predicate to determine retry conditions
 * based on the execution context and exception details.
 */
public interface RetryConfig {

  /** Returns maximum number of retry attempts. */
  int getMaxAttempts();

  /** Returns the initial backoff duration. */
  Duration getInitialBackoff();

  /** Returns the maximum backoff duration. */
  Duration getMaxBackoff();

  /** Returns the backoff multiplier for exponential backoff. */
  double getBackoffMultiplier();

  /**
   * Returns the predicate that determines whether to retry on a failure.
   *
   * @return predicate determining retry conditions
   */
  Filter getRetryPredicate();

  /** Returns whether to retry on all failures. */
  boolean isRetryOnAllFailures();
}
