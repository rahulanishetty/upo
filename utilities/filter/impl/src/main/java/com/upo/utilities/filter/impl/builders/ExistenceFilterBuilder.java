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

import com.upo.utilities.filter.api.ExistsFilter;
import com.upo.utilities.filter.api.Filter;
import com.upo.utilities.filter.api.MissingFilter;
import com.upo.utilities.filter.impl.*;

/**
 * Builds evaluators for existence-based filters (EXISTS and MISSING). These filters check for the
 * presence or absence of field values in records.
 */
public class ExistenceFilterBuilder implements FilterBuilder {

  @Override
  public <Type> FilterEvaluator<Type> toEvaluator(Filter filter, FilterContext<Type> context) {
    return switch (filter.getType()) {
      case ExistsFilter.TYPE -> buildExistsEvaluator((ExistsFilter) filter, context);
      case MissingFilter.TYPE -> buildMissingEvaluator((MissingFilter) filter, context);
      default ->
          throw new IllegalArgumentException(
              "Expected existence filter but got: " + filter.getType());
    };
  }

  /**
   * Builds an evaluator that checks if a field exists and has a non-null value. The evaluator
   * returns true if the field resolves to any non-null values.
   *
   * @param filter the EXISTS filter
   * @param context context for resolving field access
   * @param <Type> type of records being evaluated
   * @return evaluator that checks field existence
   */
  private <Type> FilterEvaluator<Type> buildExistsEvaluator(
      ExistsFilter filter, FilterContext<Type> context) {
    Field<Type> field = context.resolveField(filter.getField());
    return record -> {
      Collection<ComparableValue<?>> values = field.resolveValues(record);
      return !values.isEmpty() && values.stream().anyMatch(Objects::nonNull);
    };
  }

  /**
   * Builds an evaluator that checks if a field is missing or has null value. The evaluator returns
   * true if the field doesn't exist or resolves to null values only.
   *
   * @param filter the MISSING filter
   * @param context context for resolving field access
   * @param <Type> type of records being evaluated
   * @return evaluator that checks field absence
   */
  private <Type> FilterEvaluator<Type> buildMissingEvaluator(
      MissingFilter filter, FilterContext<Type> context) {
    Field<Type> field = context.resolveField(filter.getField());
    return record -> {
      Collection<ComparableValue<?>> values = field.resolveValues(record);
      return values.isEmpty() || values.stream().allMatch(Objects::isNull);
    };
  }
}
