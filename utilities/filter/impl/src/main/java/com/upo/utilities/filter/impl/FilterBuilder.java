/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl;

import com.upo.utilities.filter.api.Filter;

/**
 * Builder interface for constructing and transforming filters into executable evaluators. This
 * interface provides mechanisms for both filter optimization (through rewriting) and conversion of
 * logical filter definitions into executable evaluators.
 */
public interface FilterBuilder {

  /**
   * Rewrites a filter to an optimized form using the provided context. This optional step allows
   * for filter transformations such as:
   *
   * <ul>
   *   <li>Optimization of filter conditions
   *   <li>Combining or simplifying multiple filters
   *   <li>Reordering conditions for better performance
   * </ul>
   *
   * @param filter the filter to be rewritten
   * @param context the context providing filter-related capabilities
   * @return the rewritten filter, or the original filter if no rewriting is needed
   */
  default Filter rewrite(Filter filter, FilterContext<?> context) {
    return filter;
  }

  /**
   * Converts a logical filter definition into an executable evaluator. This method transforms the
   * declarative filter structure into a form that can efficiently evaluate records.
   *
   * @param <Type> the type of records this evaluator will process
   * @param filter the filter to convert
   * @param context the context providing filter-related capabilities
   * @return an evaluator that can process records according to the filter conditions
   */
  <Type> FilterEvaluator<Type> toEvaluator(Filter filter, FilterContext<Type> context);
}
