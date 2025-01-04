/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl;

import java.util.HashMap;
import java.util.Map;

import com.upo.utilities.filter.api.Filter;
import com.upo.utilities.filter.impl.builders.*;

/**
 * Registry that manages filter builders and constructs evaluators. This class acts as a central
 * point for building filter evaluators, delegating to appropriate builders based on filter type.
 */
public class FilterBuilderRegistry {
  private static final FilterBuilderRegistry INSTANCE = new FilterBuilderRegistry();

  private final Map<String, FilterBuilder> builders;

  public FilterBuilderRegistry() {
   // Initialize specialized builders
    Map<String, FilterBuilder> specializedBuilders = new HashMap<>();

   // Equality filters (EQUALS, IN)
    EqualityFilterBuilder equalityBuilder = new EqualityFilterBuilder();
    specializedBuilders.put("EQUALS", equalityBuilder);
    specializedBuilders.put("IN", equalityBuilder);

   // Comparison filters (GT, LT, GTE, LTE)
    ComparisonFilterBuilder comparisonBuilder = new ComparisonFilterBuilder();
    specializedBuilders.put("GT", comparisonBuilder);
    specializedBuilders.put("LT", comparisonBuilder);
    specializedBuilders.put("GTE", comparisonBuilder);
    specializedBuilders.put("LTE", comparisonBuilder);

   // Add Range filter builder
    RangeFilterBuilder rangeBuilder = new RangeFilterBuilder();
    specializedBuilders.put("RANGE", rangeBuilder);

   // Text match filters (REGEX, IREGEX, CONTAINS, ICONTAINS)
    TextMatchFilterBuilder textMatchBuilder = new TextMatchFilterBuilder();
    specializedBuilders.put("REGEX", textMatchBuilder);
    specializedBuilders.put("IREGEX", textMatchBuilder);
    specializedBuilders.put("CONTAINS", textMatchBuilder);
    specializedBuilders.put("ICONTAINS", textMatchBuilder);

   // Existence filters (EXISTS, MISSING)
    ExistenceFilterBuilder existenceBuilder = new ExistenceFilterBuilder();
    specializedBuilders.put("EXISTS", existenceBuilder);
    specializedBuilders.put("MISSING", existenceBuilder);

   // Initialize logical builder with all specialized builders
    LogicalFilterBuilder logicalBuilder = new LogicalFilterBuilder(specializedBuilders);

   // Add logical operators to the main builder map
    this.builders = new HashMap<>(specializedBuilders);
    this.builders.put("AND", logicalBuilder);
    this.builders.put("OR", logicalBuilder);
    this.builders.put("NOT", logicalBuilder);
  }

  /**
   * Returns the singleton instance of the registry.
   *
   * @return the singleton instance
   */
  public static FilterBuilderRegistry getInstance() {
    return INSTANCE;
  }

  /**
   * Builds an evaluator for the given filter using the appropriate builder.
   *
   * @param filter the filter to build an evaluator for
   * @param context context for resolving field access
   * @param <Type> type of records being evaluated
   * @return evaluator for the filter
   * @throws IllegalArgumentException if no builder is found for the filter type
   */
  public <Type> FilterEvaluator<Type> buildEvaluator(Filter filter, FilterContext<Type> context) {
    Filter rewrittenFilter = rewrite(filter, context);
    FilterBuilder builder = builders.get(rewrittenFilter.getType());
    if (builder == null) {
      throw new IllegalArgumentException("No builder found for filter type: " + filter.getType());
    }
    return builder.toEvaluator(rewrittenFilter, context);
  }

  /**
   * Rewrites the filter using the appropriate builder's rewrite logic. This step allows for filter
   * optimization before building the evaluator.
   *
   * @param filter the filter to rewrite
   * @param context context for resolving field access
   * @return the rewritten filter
   */
  private Filter rewrite(Filter filter, FilterContext<?> context) {
    FilterBuilder builder = builders.get(filter.getType());
    if (builder == null) {
      throw new IllegalArgumentException("No builder found for filter type: " + filter.getType());
    }
    return builder.rewrite(filter, context);
  }
}
