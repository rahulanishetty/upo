/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.upo.utilities.filter.api.*;
import com.upo.utilities.filter.impl.TestHelpers.TestFilterContext;
import com.upo.utilities.filter.impl.TestHelpers.TestRecord;
import com.upo.utilities.filter.impl.builders.TextMatchFilterBuilder;

public class TextMatchFilterBuilderTest {

  private final TextMatchFilterBuilder builder = new TextMatchFilterBuilder();
  private final FilterContext<TestRecord> context = new TestFilterContext<>();

  @Test
  void regex_ShouldMatchPattern() {
    Filter filter = new RegexFilter("email", ".*@gmail\\.com");
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertTrue(evaluator.evaluate(new TestRecord().put("email", "test@gmail.com")));
    assertFalse(evaluator.evaluate(new TestRecord().put("email", "test@yahoo.com")));
  }

  @Test
  void regex_ShouldHandleComplexPatterns() {
    Filter filter = new RegexFilter("code", "^[A-Z]{2}-\\d{3}$");
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertTrue(evaluator.evaluate(new TestRecord().put("code", "AB-123")));
    assertFalse(evaluator.evaluate(new TestRecord().put("code", "AB123")));
    assertFalse(evaluator.evaluate(new TestRecord().put("code", "ab-123")));
  }

  @Test
  void iregex_ShouldMatchPatternCaseInsensitive() {
    Filter filter = new InsensitiveRegexFilter("name", "john.*");
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertTrue(evaluator.evaluate(new TestRecord().put("name", "John Doe")));
    assertTrue(evaluator.evaluate(new TestRecord().put("name", "JOHN Smith")));
    assertTrue(evaluator.evaluate(new TestRecord().put("name", "johnny")));
    assertFalse(evaluator.evaluate(new TestRecord().put("name", "Jane Doe")));
  }

  @Test
  void contains_ShouldMatchSubstring() {
    Filter filter = new ContainsFilter("description", "important");
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertTrue(evaluator.evaluate(new TestRecord().put("description", "This is important info")));
    assertTrue(evaluator.evaluate(new TestRecord().put("description", "important")));
    assertFalse(evaluator.evaluate(new TestRecord().put("description", "This is IMPORTANT info")));
    assertFalse(evaluator.evaluate(new TestRecord().put("description", "Not relevant")));
  }

  @Test
  void icontains_ShouldMatchSubstringCaseInsensitive() {
    Filter filter = new InsensitiveContainsFilter("description", "important");
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertTrue(evaluator.evaluate(new TestRecord().put("description", "This is important info")));
    assertTrue(evaluator.evaluate(new TestRecord().put("description", "This is IMPORTANT info")));
    assertTrue(evaluator.evaluate(new TestRecord().put("description", "ImPoRtAnT")));
    assertFalse(evaluator.evaluate(new TestRecord().put("description", "Not relevant")));
  }

  @Test
  void shouldHandleNullValues() {
    Filter filter = new ContainsFilter("description", "test");
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertFalse(evaluator.evaluate(new TestRecord()));// missing field
    assertFalse(evaluator.evaluate(new TestRecord().put("description", null)));// null value
  }

  @Test
  void shouldHandleEmptyValues() {
    Filter filter = new ContainsFilter("description", "");
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertTrue(
        evaluator.evaluate(
            new TestRecord().put("description", "any text")));// empty string matches anything
    assertTrue(
        evaluator.evaluate(new TestRecord().put("description", "")));// empty string matches empty
  }

  @Test
  void shouldThrowExceptionForInvalidRegex() {
    Filter filter = new RegexFilter("field", "[invalid regex");
    assertThrows(IllegalArgumentException.class, () -> builder.toEvaluator(filter, context));
  }

  @Test
  void shouldThrowExceptionForUnsupportedFilter() {
    Filter filter = new MatchAllFilter();
    assertThrows(IllegalArgumentException.class, () -> builder.toEvaluator(filter, context));
  }
}
