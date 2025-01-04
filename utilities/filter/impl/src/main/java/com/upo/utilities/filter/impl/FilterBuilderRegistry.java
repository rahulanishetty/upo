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

import com.upo.utilities.filter.api.*;
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
    specializedBuilders.put(EqualsFilter.TYPE, equalityBuilder);
    specializedBuilders.put(InFilter.TYPE, equalityBuilder);

   // Comparison filters (GT, LT, GTE, LTE)
    ComparisonFilterBuilder comparisonBuilder = new ComparisonFilterBuilder();
    specializedBuilders.put(GreaterThanFilter.TYPE, comparisonBuilder);
    specializedBuilders.put(LessThanFilter.TYPE, comparisonBuilder);
    specializedBuilders.put(GreaterThanEqualsFilter.TYPE, comparisonBuilder);
    specializedBuilders.put(LessThanEqualsFilter.TYPE, comparisonBuilder);

   // Add Range filter builder
    RangeFilterBuilder rangeBuilder = new RangeFilterBuilder();
    specializedBuilders.put(RangeFilter.TYPE, rangeBuilder);

   // Text match filters (REGEX, IREGEX, CONTAINS, ICONTAINS)
    TextMatchFilterBuilder textMatchBuilder = new TextMatchFilterBuilder();
    specializedBuilders.put(RegexFilter.TYPE, textMatchBuilder);
    specializedBuilders.put(InsensitiveRegexFilter.TYPE, textMatchBuilder);
    specializedBuilders.put(ContainsFilter.TYPE, textMatchBuilder);
    specializedBuilders.put(InsensitiveContainsFilter.TYPE, textMatchBuilder);

   // Existence filters (EXISTS, MISSING)
    ExistenceFilterBuilder existenceBuilder = new ExistenceFilterBuilder();
    specializedBuilders.put(ExistsFilter.TYPE, existenceBuilder);
    specializedBuilders.put(MissingFilter.TYPE, existenceBuilder);

   // Add Match filter builder
    MatchFilterBuilder matchBuilder = new MatchFilterBuilder();
    specializedBuilders.put(MatchAllFilter.TYPE, matchBuilder);
    specializedBuilders.put(MatchNoneFilter.TYPE, matchBuilder);

   // Initialize logical builder with all specialized builders
    LogicalFilterBuilder logicalBuilder = new LogicalFilterBuilder(specializedBuilders);

   // Add logical operators to the main builder map
    this.builders = new HashMap<>(specializedBuilders);
    this.builders.put(AndFilter.TYPE, logicalBuilder);
    this.builders.put(OrFilter.TYPE, logicalBuilder);
    this.builders.put(NotFilter.TYPE, logicalBuilder);
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
    if (rewrittenFilter == null) {
      return type -> false;
    }
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
    if (filter == null) {
      return null;
    }
    if (context == null) {
      throw new IllegalArgumentException("Context cannot be null");
    }
    FilterBuilder builder = builders.get(filter.getType());
    if (builder == null) {
      throw new IllegalArgumentException("No builder found for filter type: " + filter.getType());
    }
    return builder.rewrite(filter, context);
  }
}
