/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl.builders;

import java.util.Collection;
import java.util.function.Predicate;

import com.upo.utilities.filter.api.*;
import com.upo.utilities.filter.impl.*;

/**
 * Builds evaluators for comparison-based filters (GT, LT, GTE, LTE). This builder creates
 * evaluators that perform ordered comparisons between field values and a specified comparison
 * value.
 */
public class ComparisonFilterBuilder implements FilterBuilder {

  @Override
  public <Type> FilterEvaluator<Type> toEvaluator(Filter filter, FilterContext<Type> context) {
    if (!(filter instanceof ComparisonFilter compFilter)) {
      throw new IllegalArgumentException("Expected ComparisonFilter but got: " + filter.getType());
    }

    Field<Type> field = context.resolveField(compFilter.getField());
    ComparableValue<?> compareValue = field.toComparable(compFilter.getValue());

    return switch (filter.getType()) {
      case GreaterThanFilter.TYPE ->
          record -> evaluateComparison(field.resolveValues(record), compareValue, v -> v > 0);
      case LessThanFilter.TYPE ->
          record -> evaluateComparison(field.resolveValues(record), compareValue, v -> v < 0);
      case GreaterThanEqualsFilter.TYPE ->
          record -> evaluateComparison(field.resolveValues(record), compareValue, v -> v >= 0);
      case LessThanEqualsFilter.TYPE ->
          record -> evaluateComparison(field.resolveValues(record), compareValue, v -> v <= 0);
      default -> throw new IllegalArgumentException("Unknown comparison type: " + filter.getType());
    };
  }

  /**
   * Evaluates ordered comparisons between field values and a comparison value. This method applies
   * the provided comparison predicate to determine if any field value satisfies the comparison
   * condition.
   *
   * @param values the collection of field values to compare
   * @param compareValue the value to compare against
   * @param comparator predicate that defines the comparison logic
   * @param <T> the type of values being compared
   * @return true if any field value satisfies the comparison condition
   * @implNote Uses raw types and suppresses warnings due to generic type erasure in comparisons.
   *     This is necessary because the actual comparable types are not known at runtime.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private <T> boolean evaluateComparison(
      Collection<ComparableValue<?>> values,
      ComparableValue compareValue,
      Predicate<Integer> comparator) {
    return values.stream()
        .anyMatch(
            value -> {
              return comparator.test(compareValue.compareTo(value));
            });
  }
}
