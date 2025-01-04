/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl.builders;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.upo.utilities.filter.api.Filter;
import com.upo.utilities.filter.api.NestedFilter;
import com.upo.utilities.filter.impl.*;

/**
 * Builds evaluators for nested array filtering, maintaining context within each array element. This
 * builder supports filtering on nested arrays where conditions need to match within the same array
 * element, using a type-safe context chain.
 */
public class NestedFilterBuilder implements FilterBuilder {
  private final Map<String, FilterBuilder> delegates;

  public NestedFilterBuilder(Map<String, FilterBuilder> delegates) {
    this.delegates = delegates;
  }

  /**
   * Creates an evaluator for a nested filter. The evaluator checks if any element in the nested
   * array matches all the specified conditions.
   *
   * @param filter the nested filter to create an evaluator for
   * @param context context for resolving fields in the parent object
   * @param <Type> type of records being evaluated
   * @return evaluator that checks conditions on nested array elements
   * @throws IllegalArgumentException if the filter is not a NestedFilter, if the specified field is
   *     not a NestedField, or if no builder is found for any nested condition
   */
  @Override
  public <Type> FilterEvaluator<Type> toEvaluator(Filter filter, FilterContext<Type> context) {
    if (!(filter instanceof NestedFilter nestedFilter)) {
      throw new IllegalArgumentException("Expected NestedFilter but got: " + filter.getType());
    }

   // Resolve the array field
    Field<Type> field = context.resolveField(nestedFilter.getArrayField());
    if (!(field instanceof NestedField<Type> nestedField)) {
      throw new IllegalArgumentException(
          "Field must be a NestedField for nested filtering: " + nestedFilter.getArrayField());
    }

   // Create a properly typed context for the nested elements
    FilterContext<?> innerContext = nestedField.getInnerContext(context);

   // Build evaluators for all child filters using the inner context
    List<FilterEvaluator<?>> childEvaluators =
        buildChildEvaluators(nestedFilter.getFilters(), innerContext);

    return record -> {
      Collection<?> arrayElements = nestedField.resolveInnerObjects(record);
      if (arrayElements == null || arrayElements.isEmpty()) {
        return false;
      }

     //noinspection unchecked
      return arrayElements.stream()
          .anyMatch(
              element ->
                  childEvaluators.stream()
                      .allMatch(
                          evaluator -> ((FilterEvaluator<Object>) evaluator).evaluate(element)));
    };
  }

  /**
   * Builds evaluators for all conditions that need to be checked on the nested elements.
   *
   * @param filters list of filters to build evaluators for
   * @param context context for resolving fields in the nested elements
   * @return list of evaluators for the nested conditions
   * @throws IllegalArgumentException if no builder is found for any filter type
   */
  private List<FilterEvaluator<?>> buildChildEvaluators(
      List<Filter> filters, FilterContext<?> context) {
    return filters.stream()
        .map(filter -> buildChildEvaluator(filter, context))
        .collect(Collectors.toList());
  }

  /**
   * Builds an evaluator for a single condition using the appropriate delegate builder.
   *
   * @param filter the filter to build an evaluator for
   * @param context context for resolving fields in the nested elements
   * @return evaluator for the condition
   * @throws IllegalArgumentException if no builder is found for the filter type
   */
  private FilterEvaluator<?> buildChildEvaluator(Filter filter, FilterContext<?> context) {
    FilterBuilder builder = delegates.get(filter.getType());
    if (builder == null) {
      throw new IllegalArgumentException("No builder found for filter type: " + filter.getType());
    }
    return builder.toEvaluator(filter, context);
  }
}
