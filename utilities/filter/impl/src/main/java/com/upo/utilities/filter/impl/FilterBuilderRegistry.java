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
    this.builders = new HashMap<>();

   // Equality filters (EQUALS, IN)
    EqualityFilterBuilder equalityBuilder = new EqualityFilterBuilder();
    this.builders.put(EqualsFilter.TYPE, equalityBuilder);
    this.builders.put(InFilter.TYPE, equalityBuilder);

   // Comparison filters (GT, LT, GTE, LTE)
    ComparisonFilterBuilder comparisonBuilder = new ComparisonFilterBuilder();
    this.builders.put(GreaterThanFilter.TYPE, comparisonBuilder);
    this.builders.put(LessThanFilter.TYPE, comparisonBuilder);
    this.builders.put(GreaterThanEqualsFilter.TYPE, comparisonBuilder);
    this.builders.put(LessThanEqualsFilter.TYPE, comparisonBuilder);

   // Add Range filter builder
    RangeFilterBuilder rangeBuilder = new RangeFilterBuilder();
    this.builders.put(RangeFilter.TYPE, rangeBuilder);

   // Text match filters (REGEX, IREGEX, CONTAINS, ICONTAINS)
    TextMatchFilterBuilder textMatchBuilder = new TextMatchFilterBuilder();
    this.builders.put(RegexFilter.TYPE, textMatchBuilder);
    this.builders.put(InsensitiveRegexFilter.TYPE, textMatchBuilder);
    this.builders.put(ContainsFilter.TYPE, textMatchBuilder);
    this.builders.put(InsensitiveContainsFilter.TYPE, textMatchBuilder);

   // Existence filters (EXISTS, MISSING)
    ExistenceFilterBuilder existenceBuilder = new ExistenceFilterBuilder();
    this.builders.put(ExistsFilter.TYPE, existenceBuilder);
    this.builders.put(MissingFilter.TYPE, existenceBuilder);

   // Add Match filter builder
    MatchFilterBuilder matchBuilder = new MatchFilterBuilder();
    this.builders.put(MatchAllFilter.TYPE, matchBuilder);
    this.builders.put(MatchNoneFilter.TYPE, matchBuilder);

   // Initialize Nested builder with all builders
    NestedFilterBuilder nestedFilterBuilder = new NestedFilterBuilder(this.builders);
    this.builders.put(NestedFilter.TYPE, nestedFilterBuilder);

   // Initialize logical builder with all builders
    LogicalFilterBuilder logicalBuilder = new LogicalFilterBuilder(this.builders);
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

  public static FilterBuilder getFilterBuilder(String type) {
    return INSTANCE.builders.get(type);
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
