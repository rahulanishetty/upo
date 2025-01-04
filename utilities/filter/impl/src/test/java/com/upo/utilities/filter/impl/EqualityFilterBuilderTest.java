/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.upo.utilities.filter.api.EqualsFilter;
import com.upo.utilities.filter.api.Filter;
import com.upo.utilities.filter.api.InFilter;
import com.upo.utilities.filter.api.MatchAllFilter;
import com.upo.utilities.filter.impl.TestHelpers.TestRecord;
import com.upo.utilities.filter.impl.builders.EqualityFilterBuilder;

public class EqualityFilterBuilderTest {

  private final EqualityFilterBuilder builder = new EqualityFilterBuilder();
  private final FilterContext<TestRecord> context = new TestHelpers.TestFilterContext<>();

  @Test
  void equalsFilter_ShouldMatchEqualValue() {
    Filter filter = new EqualsFilter("age", 25);
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    TestRecord record = new TestRecord().put("age", 25);
    assertTrue(evaluator.evaluate(record));
  }

  @Test
  void equalsFilter_ShouldNotMatchDifferentValue() {
    Filter filter = new EqualsFilter("age", 25);
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    TestRecord record = new TestRecord().put("age", 30);
    assertFalse(evaluator.evaluate(record));
  }

  @Test
  void equalsFilter_ShouldNotMatchMissingField() {
    Filter filter = new EqualsFilter("age", 25);
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    TestRecord record = new TestRecord();
    assertFalse(evaluator.evaluate(record));
  }

  @Test
  void inFilter_ShouldMatchValueInSet() {
    Filter filter = new InFilter("status", Arrays.asList("ACTIVE", "PENDING"));
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    TestRecord record = new TestRecord().put("status", "ACTIVE");
    assertTrue(evaluator.evaluate(record));
  }

  @Test
  void inFilter_ShouldNotMatchValueNotInSet() {
    Filter filter = new InFilter("status", Arrays.asList("ACTIVE", "PENDING"));
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    TestRecord record = new TestRecord().put("status", "INACTIVE");
    assertFalse(evaluator.evaluate(record));
  }

  @Test
  void shouldThrowExceptionForUnsupportedFilter() {
    Filter filter = new MatchAllFilter();
    assertThrows(IllegalArgumentException.class, () -> builder.toEvaluator(filter, context));
  }
}
