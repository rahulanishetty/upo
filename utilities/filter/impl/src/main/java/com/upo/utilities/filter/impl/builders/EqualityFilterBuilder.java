/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl.builders;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.upo.utilities.filter.api.EqualsFilter;
import com.upo.utilities.filter.api.Filter;
import com.upo.utilities.filter.api.InFilter;
import com.upo.utilities.filter.impl.*;

/**
 * Builds evaluators for equality-based filters (EQUALS and IN). This builder creates evaluators
 * that compare field values with provided values using either exact equality or set membership.
 */
public class EqualityFilterBuilder implements FilterBuilder {

  @Override
  public <Type> FilterEvaluator<Type> toEvaluator(Filter filter, FilterContext<Type> context) {
    return switch (filter.getType()) {
      case EqualsFilter.TYPE -> buildEqualsEvaluator((EqualsFilter) filter, context);
      case InFilter.TYPE -> buildInEvaluator((InFilter) filter, context);
      default -> throw new IllegalArgumentException("Unsupported filter type: " + filter.getType());
    };
  }

  /**
   * Builds an evaluator for exact equality comparison. The resulting evaluator checks if any value
   * of the specified field exactly matches the comparison value.
   *
   * @param filter the equals filter containing field and comparison value
   * @param context context for resolving field access
   * @param <Type> type of records being evaluated
   * @return evaluator that performs equality comparison
   */
  private <Type> FilterEvaluator<Type> buildEqualsEvaluator(
      EqualsFilter filter, FilterContext<Type> context) {
    Field<Type> field = context.resolveField(filter.getField());
    ComparableValue<?> compareValue = field.toComparable(filter.getValue());

    return record -> {
      Collection<ComparableValue<?>> values = field.resolveValues(record);
      return values.stream().anyMatch(value -> value.equals(compareValue));
    };
  }

  /**
   * Builds an evaluator for set membership checks. The resulting evaluator checks if any value of
   * the specified field exists within the set of provided comparison values.
   *
   * @param filter the in filter containing field and set of comparison values
   * @param context context for resolving field access
   * @param <Type> type of records being evaluated
   * @return evaluator that performs set membership checks
   */
  private <Type> FilterEvaluator<Type> buildInEvaluator(
      InFilter filter, FilterContext<Type> context) {
    Field<Type> field = context.resolveField(filter.getField());
    Set<ComparableValue<?>> compareValues =
        filter.getValues().stream()
            .map(field::toComparable)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    return record -> {
      Collection<ComparableValue<?>> values = field.resolveValues(record);
      return values.stream().anyMatch(compareValues::contains);
    };
  }
}
