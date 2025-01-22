/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl.builders;

import java.util.Collection;

import com.upo.utilities.filter.api.Filter;
import com.upo.utilities.filter.api.RangeFilter;
import com.upo.utilities.filter.impl.*;

/**
 * Builds evaluators for range-based filters. Handles both inclusive and exclusive bounds for range
 * comparison, supporting: - Lower and upper bounds can each be inclusive or exclusive - Open-ended
 * ranges (either bound can be null) - Any comparable value types
 */
public class RangeFilterBuilder implements FilterBuilder {

  @Override
  public <Type> FilterEvaluator<Type> toEvaluator(Filter filter, FilterContext<Type> context) {
    if (!(filter instanceof RangeFilter rangeFilter)) {
      throw new IllegalArgumentException("Expected RangeFilter but got: " + filter.getType());
    }

    Field<Type> field = context.resolveField(rangeFilter.getField());
    ComparableValue<?> fromValue =
        rangeFilter.getFrom() != null ? field.toComparable(rangeFilter.getFrom()) : null;
    ComparableValue<?> toValue =
        rangeFilter.getTo() != null ? field.toComparable(rangeFilter.getTo()) : null;

    return record -> {
     // Resolve record-aware values if needed
     //noinspection unchecked
      var resolvedFromValue =
          fromValue instanceof RecordAware<?> fromAware
              ? ((RecordAware<Type>) fromAware).resolve(record)
              : fromValue;
     //noinspection unchecked
      var resolvedToValue =
          toValue instanceof RecordAware<?> toAware
              ? ((RecordAware<Type>) toAware).resolve(record)
              : toValue;

      return evaluateRange(
          field.resolveValues(record),
          resolvedFromValue,
          resolvedToValue,
          rangeFilter.isFromInclusive(),
          rangeFilter.isToInclusive());
    };
  }

  /**
   * Evaluates whether any of the field values fall within the specified range. The range can be: -
   * Bounded on both ends: value must be between fromValue and toValue - Left-bounded only: value
   * must be greater than (or equal to) fromValue - Right-bounded only: value must be less than (or
   * equal to) toValue
   *
   * @param values the collection of field values to check
   * @param fromValue lower bound of the range, or null if unbounded
   * @param toValue upper bound of the range, or null if unbounded
   * @param fromInclusive whether the lower bound is inclusive
   * @param toInclusive whether the upper bound is inclusive
   * @return true if any value falls within the range
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private boolean evaluateRange(
      Collection<ComparableValue<?>> values,
      ComparableValue<?> fromValue,
      ComparableValue<?> toValue,
      boolean fromInclusive,
      boolean toInclusive) {

    return values.stream()
        .anyMatch(
            value -> {
             // Check lower bound if it exists
              if (fromValue != null) {
                int compareFrom = ((Comparable) value).compareTo(fromValue);
                if (fromInclusive && compareFrom < 0) return false;
                if (!fromInclusive && compareFrom <= 0) return false;
              }

             // Check upper bound if it exists
              if (toValue != null) {
                int compareTo = ((Comparable) value).compareTo(toValue);
                if (toInclusive && compareTo > 0) return false;
                if (!toInclusive && compareTo >= 0) return false;
              }

              return true;
            });
  }
}
