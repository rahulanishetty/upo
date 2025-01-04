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
import com.upo.utilities.filter.impl.builders.ComparisonFilterBuilder;

public class ComparisonFilterBuilderTest {
  private final ComparisonFilterBuilder builder = new ComparisonFilterBuilder();
  private final FilterContext<TestRecord> context = new TestFilterContext<>();

  @Test
  void greaterThan_ShouldMatchGreaterValue() {
    Filter filter = new GreaterThanFilter("age", 18);
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    TestRecord record = new TestRecord().put("age", 25);
    assertTrue(evaluator.evaluate(record));
  }

  @Test
  void greaterThan_ShouldNotMatchLesserValue() {
    Filter filter = new GreaterThanFilter("age", 18);
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    TestRecord record = new TestRecord().put("age", 15);
    assertFalse(evaluator.evaluate(record));
  }

  @Test
  void greaterThanEquals_ShouldMatchEqualValue() {
    Filter filter = new GreaterThanEqualsFilter("age", 18);
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    TestRecord record = new TestRecord().put("age", 18);
    assertTrue(evaluator.evaluate(record));
  }

  @Test
  void lessThan_ShouldMatchLesserValue() {
    Filter filter = new LessThanFilter("age", 18);
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    TestRecord record = new TestRecord().put("age", 15);
    assertTrue(evaluator.evaluate(record));
  }

  @Test
  void lessThanEquals_ShouldMatchEqualValue() {
    Filter filter = new LessThanEqualsFilter("age", 18);
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    TestRecord record = new TestRecord().put("age", 18);
    assertTrue(evaluator.evaluate(record));
  }

  @Test
  void shouldHandleNullValues() {
    Filter filter = new GreaterThanFilter("age", 18);
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    TestRecord record = new TestRecord();
    assertFalse(evaluator.evaluate(record));
  }

  @Test
  void shouldThrowExceptionForUnsupportedFilter() {
    Filter filter = new MatchAllFilter();
    assertThrows(IllegalArgumentException.class, () -> builder.toEvaluator(filter, context));
  }
}
