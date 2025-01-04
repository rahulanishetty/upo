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

import com.upo.utilities.filter.api.Filter;
import com.upo.utilities.filter.api.MatchAllFilter;
import com.upo.utilities.filter.api.RangeFilter;
import com.upo.utilities.filter.impl.TestHelpers.TestRecord;
import com.upo.utilities.filter.impl.builders.RangeFilterBuilder;

public class RangeFilterBuilderTest {
  private final RangeFilterBuilder builder = new RangeFilterBuilder();
  private final FilterContext<TestRecord> context = new TestHelpers.TestFilterContext<>();

  @Test
  void shouldMatchValueInInclusiveRange() {
    Filter filter = new RangeFilter("age", 18, 65, true, true);
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertTrue(evaluator.evaluate(new TestRecord().put("age", 18)));// lower bound
    assertTrue(evaluator.evaluate(new TestRecord().put("age", 40)));// middle
    assertTrue(evaluator.evaluate(new TestRecord().put("age", 65)));// upper bound
  }

  @Test
  void shouldNotMatchValueOutsideInclusiveRange() {
    Filter filter = new RangeFilter("age", 18, 65, true, true);
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertFalse(evaluator.evaluate(new TestRecord().put("age", 17)));// below
    assertFalse(evaluator.evaluate(new TestRecord().put("age", 66)));// above
  }

  @Test
  void shouldHandleExclusiveBounds() {
    Filter filter = new RangeFilter("age", 18, 65, false, false);
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertFalse(evaluator.evaluate(new TestRecord().put("age", 18)));// lower bound
    assertTrue(evaluator.evaluate(new TestRecord().put("age", 40)));// middle
    assertFalse(evaluator.evaluate(new TestRecord().put("age", 65)));// upper bound
  }

  @Test
  void shouldHandleMixedBounds() {
    Filter filter = new RangeFilter("age", 18, 65, true, false);
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertTrue(evaluator.evaluate(new TestRecord().put("age", 18)));// lower bound (inclusive)
    assertTrue(evaluator.evaluate(new TestRecord().put("age", 40)));// middle
    assertFalse(evaluator.evaluate(new TestRecord().put("age", 65)));// upper bound (exclusive)
  }

  @Test
  void shouldHandleUnboundedRanges() {
   // Only lower bound
    Filter lowerBound = new RangeFilter("age", 18, null, true, false);
    FilterEvaluator<TestRecord> lowerEvaluator = builder.toEvaluator(lowerBound, context);
    assertTrue(lowerEvaluator.evaluate(new TestRecord().put("age", 100)));

   // Only upper bound
    Filter upperBound = new RangeFilter("age", null, 65, false, true);
    FilterEvaluator<TestRecord> upperEvaluator = builder.toEvaluator(upperBound, context);
    assertTrue(upperEvaluator.evaluate(new TestRecord().put("age", 1)));
  }

  @Test
  void shouldHandleNullValues() {
    Filter filter = new RangeFilter("age", 18, 65, true, true);
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertFalse(evaluator.evaluate(new TestRecord()));// missing field
    assertFalse(evaluator.evaluate(new TestRecord().put("age", null)));// null value
  }

  @Test
  void shouldThrowExceptionForUnsupportedFilter() {
    Filter filter = new MatchAllFilter();
    assertThrows(IllegalArgumentException.class, () -> builder.toEvaluator(filter, context));
  }
}
