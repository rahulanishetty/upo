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
import com.upo.utilities.filter.api.ExistsFilter;
import com.upo.utilities.filter.api.Filter;
import com.upo.utilities.filter.api.MissingFilter;
import com.upo.utilities.filter.impl.TestHelpers.TestFilterContext;
import com.upo.utilities.filter.impl.TestHelpers.TestRecord;
import com.upo.utilities.filter.impl.builders.ExistenceFilterBuilder;

public class ExistenceFilterBuilderTest {
  private final ExistenceFilterBuilder builder = new ExistenceFilterBuilder();
  private final FilterContext<TestRecord> context = new TestFilterContext<>();

  @Test
  void exists_ShouldMatchExistingNonNullField() {
    Filter filter = new ExistsFilter("field");
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertTrue(evaluator.evaluate(new TestRecord().put("field", "value")));
    assertTrue(evaluator.evaluate(new TestRecord().put("field", 0)));
    assertTrue(evaluator.evaluate(new TestRecord().put("field", false)));
  }

  @Test
  void exists_ShouldNotMatchMissingOrNullField() {
    Filter filter = new ExistsFilter("field");
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertFalse(evaluator.evaluate(new TestRecord()));// missing field
    assertFalse(evaluator.evaluate(new TestRecord().put("field", null)));// null value
  }

  @Test
  void exists_ShouldCheckSpecificField() {
    Filter filter = new ExistsFilter("field1");
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertFalse(evaluator.evaluate(new TestRecord().put("field2", "value")));// different field
    assertTrue(
        evaluator.evaluate(
            new TestRecord().put("field1", "value").put("field2", null)));// specific field exists
  }

  @Test
  void missing_ShouldMatchMissingOrNullField() {
    Filter filter = new MissingFilter("field");
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertTrue(evaluator.evaluate(new TestRecord()));// missing field
    assertTrue(evaluator.evaluate(new TestRecord().put("field", null)));// null value
  }

  @Test
  void missing_ShouldNotMatchExistingField() {
    Filter filter = new MissingFilter("field");
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertFalse(evaluator.evaluate(new TestRecord().put("field", "value")));
    assertFalse(evaluator.evaluate(new TestRecord().put("field", 0)));
    assertFalse(evaluator.evaluate(new TestRecord().put("field", false)));
  }

  @Test
  void missing_ShouldCheckSpecificField() {
    Filter filter = new MissingFilter("field1");
    FilterEvaluator<TestRecord> evaluator = builder.toEvaluator(filter, context);

    assertTrue(evaluator.evaluate(new TestRecord().put("field2", "value")));// different field
    assertFalse(
        evaluator.evaluate(
            new TestRecord().put("field1", "value").put("field2", null)));// specific field exists
  }

  @Test
  void shouldThrowExceptionForUnsupportedFilter() {
    Filter filter = new EqualsFilter("field", "value");
    assertThrows(IllegalArgumentException.class, () -> builder.toEvaluator(filter, context));
  }
}
