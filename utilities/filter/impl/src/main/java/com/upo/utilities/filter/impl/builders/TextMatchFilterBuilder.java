/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl.builders;

import java.util.regex.Pattern;

import com.upo.utilities.filter.api.*;
import com.upo.utilities.filter.impl.Field;
import com.upo.utilities.filter.impl.FilterBuilder;
import com.upo.utilities.filter.impl.FilterContext;
import com.upo.utilities.filter.impl.FilterEvaluator;

/**
 * Builds evaluators for text matching filters (REGEX, IREGEX, CONTAINS, ICONTAINS). This builder
 * creates evaluators that perform pattern matching and substring searches on text values, with
 * options for case sensitivity.
 */
public class TextMatchFilterBuilder implements FilterBuilder {

  @Override
  public <Type> FilterEvaluator<Type> toEvaluator(Filter filter, FilterContext<Type> context) {
    if (!(filter instanceof TextMatchFilter textFilter)) {
      throw new IllegalArgumentException("Expected TextMatchFilter but got: " + filter.getType());
    }

    Field<Type> field = context.resolveField(textFilter.getField());
    String pattern = textFilter.getPattern();

    return switch (filter.getType()) {
      case RegexFilter.TYPE -> buildRegexEvaluator(field, pattern, false);
      case InsensitiveRegexFilter.TYPE -> buildRegexEvaluator(field, pattern, true);
      case ContainsFilter.TYPE -> buildContainsEvaluator(field, pattern, false);
      case InsensitiveContainsFilter.TYPE -> buildContainsEvaluator(field, pattern, true);
      default -> throw new IllegalArgumentException("Unknown text match type: " + filter.getType());
    };
  }

  /**
   * Builds an evaluator that uses regular expressions for pattern matching. The evaluator compiles
   * the pattern once and reuses it for all evaluations.
   *
   * @param field the field to evaluate
   * @param pattern the regular expression pattern
   * @param caseInsensitive whether to ignore case in pattern matching
   * @param <Type> type of records being evaluated
   * @return evaluator that performs regex pattern matching
   */
  private <Type> FilterEvaluator<Type> buildRegexEvaluator(
      Field<Type> field, String pattern, boolean caseInsensitive) {
    Pattern regex = Pattern.compile(pattern, caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);

    return record ->
        field.resolveValues(record).stream()
            .map(Object::toString)
            .anyMatch(value -> regex.matcher(value).matches());
  }

  /**
   * Builds an evaluator that performs substring matching. The evaluator pre-processes the search
   * pattern for case-insensitive searches to avoid repeated case conversions of the pattern.
   *
   * @param field the field to evaluate
   * @param pattern the substring to search for
   * @param caseInsensitive whether to ignore case in substring matching
   * @param <Type> type of records being evaluated
   * @return evaluator that performs substring matching
   */
  private <Type> FilterEvaluator<Type> buildContainsEvaluator(
      Field<Type> field, String pattern, boolean caseInsensitive) {
    String searchPattern = caseInsensitive ? pattern.toLowerCase() : pattern;

    return record ->
        field.resolveValues(record).stream()
            .map(value -> caseInsensitive ? value.toString().toLowerCase() : value.toString())
            .anyMatch(value -> value.contains(searchPattern));
  }
}
