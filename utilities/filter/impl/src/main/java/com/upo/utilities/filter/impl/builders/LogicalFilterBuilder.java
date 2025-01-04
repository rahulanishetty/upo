/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl.builders;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.upo.utilities.filter.api.AndFilter;
import com.upo.utilities.filter.api.Filter;
import com.upo.utilities.filter.api.NotFilter;
import com.upo.utilities.filter.api.OrFilter;
import com.upo.utilities.filter.impl.FilterBuilder;
import com.upo.utilities.filter.impl.FilterContext;
import com.upo.utilities.filter.impl.FilterEvaluator;

/**
 * Builds evaluators for logical operations (AND, OR, NOT) by composing other filter evaluators.
 * This builder delegates the construction of child filter evaluators to appropriate builders and
 * combines their results using logical operations.
 */
public class LogicalFilterBuilder implements FilterBuilder {
  private final Map<String, FilterBuilder> delegates;

  /**
   * Creates a logical filter builder with the specified delegate builders.
   *
   * @param delegates map of filter types to their corresponding builders, used to construct
   *     evaluators for child filters
   */
  public LogicalFilterBuilder(Map<String, FilterBuilder> delegates) {
    this.delegates = delegates;
  }

  @Override
  public <Type> FilterEvaluator<Type> toEvaluator(Filter filter, FilterContext<Type> context) {
    return switch (filter.getType()) {
      case AndFilter.TYPE -> buildAndEvaluator((AndFilter) filter, context);
      case OrFilter.TYPE -> buildOrEvaluator((OrFilter) filter, context);
      case NotFilter.TYPE -> buildNotEvaluator((NotFilter) filter, context);
      default ->
          throw new IllegalArgumentException("Unknown logical operator: " + filter.getType());
    };
  }

  /**
   * Builds an evaluator that performs logical AND operation. The resulting evaluator returns true
   * only if all child evaluators return true.
   *
   * @param filter the AND filter containing child filters
   * @param context context for resolving field access
   * @param <Type> type of records being evaluated
   * @return evaluator that performs logical AND
   */
  private <Type> FilterEvaluator<Type> buildAndEvaluator(
      AndFilter filter, FilterContext<Type> context) {
    List<FilterEvaluator<Type>> evaluators = buildChildEvaluators(filter.getFilters(), context);
    return record -> evaluators.stream().allMatch(eval -> eval.evaluate(record));
  }

  /**
   * Builds an evaluator that performs logical OR operation. The resulting evaluator returns true if
   * any child evaluator returns true.
   *
   * @param filter the OR filter containing child filters
   * @param context context for resolving field access
   * @param <Type> type of records being evaluated
   * @return evaluator that performs logical OR
   */
  private <Type> FilterEvaluator<Type> buildOrEvaluator(
      OrFilter filter, FilterContext<Type> context) {
    List<FilterEvaluator<Type>> evaluators = buildChildEvaluators(filter.getFilters(), context);
    return record -> evaluators.stream().anyMatch(eval -> eval.evaluate(record));
  }

  /**
   * Builds an evaluator that performs logical NOT operation. The resulting evaluator returns the
   * negation of its child evaluator's result.
   *
   * @param filter the NOT filter containing the filter to negate
   * @param context context for resolving field access
   * @param <Type> type of records being evaluated
   * @return evaluator that performs logical NOT
   */
  private <Type> FilterEvaluator<Type> buildNotEvaluator(
      NotFilter filter, FilterContext<Type> context) {
    FilterEvaluator<Type> evaluator = buildChildEvaluator(filter.getFilter(), context);
    return record -> !evaluator.evaluate(record);
  }

  /**
   * Builds evaluators for a list of child filters.
   *
   * @param filters list of filters to build evaluators for
   * @param context context for resolving field access
   * @param <Type> type of records being evaluated
   * @return list of evaluators for the child filters
   */
  private <Type> List<FilterEvaluator<Type>> buildChildEvaluators(
      List<Filter> filters, FilterContext<Type> context) {
    return filters.stream().map(f -> buildChildEvaluator(f, context)).collect(Collectors.toList());
  }

  /**
   * Builds an evaluator for a single child filter using the appropriate delegate builder.
   *
   * @param filter the filter to build an evaluator for
   * @param context context for resolving field access
   * @param <Type> type of records being evaluated
   * @return evaluator for the child filter
   * @throws IllegalArgumentException if no builder is found for the filter type
   */
  private <Type> FilterEvaluator<Type> buildChildEvaluator(
      Filter filter, FilterContext<Type> context) {
    FilterBuilder builder = delegates.get(filter.getType());
    if (builder == null) {
      throw new IllegalArgumentException("No builder found for filter type: " + filter.getType());
    }
    return builder.toEvaluator(filter, context);
  }
}
