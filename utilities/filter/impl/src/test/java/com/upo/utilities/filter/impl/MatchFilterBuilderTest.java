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

import com.upo.utilities.filter.api.EqualsFilter;
import com.upo.utilities.filter.api.Filter;
import com.upo.utilities.filter.api.MatchAllFilter;
import com.upo.utilities.filter.api.MatchNoneFilter;
import com.upo.utilities.filter.impl.TestHelpers.TestFilterContext;
import com.upo.utilities.filter.impl.TestHelpers.TestRecord;
import com.upo.utilities.filter.impl.builders.MatchFilterBuilder;

public class MatchFilterBuilderTest {
  private final MatchFilterBuilder builder = new MatchFilterBuilder();
  private final FilterContext<TestRecord> context = new TestFilterContext<>();

  @Test
  void matchAll_ShouldMatchEveryRecord() {
    Filter filter = new MatchAllFilter();
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertTrue(evaluator.evaluate(new TestRecord()));
    assertTrue(evaluator.evaluate(new TestRecord().put("field", "value")));
    assertTrue(evaluator.evaluate(new TestRecord().put("field", null)));
  }

  @Test
  void matchNone_ShouldMatchNoRecord() {
    Filter filter = new MatchNoneFilter();
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertFalse(evaluator.evaluate(new TestRecord()));
    assertFalse(evaluator.evaluate(new TestRecord().put("field", "value")));
    assertFalse(evaluator.evaluate(new TestRecord().put("field", null)));
  }

  @Test
  void shouldThrowExceptionForUnsupportedFilter() {
    Filter filter = new EqualsFilter("field", "value");
    assertThrows(IllegalArgumentException.class, () -> builder.toEvaluator(filter, context));
  }
}
