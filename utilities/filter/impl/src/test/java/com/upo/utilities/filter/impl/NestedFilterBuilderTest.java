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
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.upo.utilities.filter.api.*;
import com.upo.utilities.filter.impl.TestHelpers.TestRecord;

public class NestedFilterBuilderTest {

  private final FilterBuilderRegistry registry = FilterBuilderRegistry.getInstance();
  private final FilterContext<TestRecord> context = new TestHelpers.TestWithNestedFilterContext<>();

  @Test
  void shouldMatchWhenAllConditionsMatchOnSameArrayElement() {
   // Create a nested filter checking for items with quantity > 5 and price < 100
    Filter nestedFilter =
        new NestedFilter(
            "items",
            Arrays.asList(
                new GreaterThanFilter("quantity", 5), new LessThanFilter("price", 100.0)));

    FilterEvaluator<TestRecord> evaluator = registry.buildEvaluator(nestedFilter, context);

   // Create test record with array of items
    TestRecord record =
        new TestRecord()
            .put(
                "items",
                Arrays.asList(
                    new TestRecord().put("quantity", 3).put("price", 50.0),// qty too low
                    new TestRecord().put("quantity", 6).put("price", 150.0),// price too high
                    new TestRecord()
                        .put("quantity", 7)
                        .put("price", 75.0)// matches both conditions
                    ));

    assertTrue(evaluator.evaluate(record));
  }

  @Test
  void shouldNotMatchWhenConditionsMatchOnDifferentArrayElements() {
    Filter nestedFilter =
        new NestedFilter(
            "items",
            Arrays.asList(
                new GreaterThanFilter("quantity", 5), new LessThanFilter("price", 100.0)));

    FilterEvaluator<TestRecord> evaluator = registry.buildEvaluator(nestedFilter, context);

   // Create test record where conditions match on different elements
    TestRecord record =
        new TestRecord()
            .put(
                "items",
                Arrays.asList(
                    new TestRecord()
                        .put("quantity", 6)
                        .put("price", 150.0),// matches quantity only
                    new TestRecord().put("quantity", 3).put("price", 75.0)// matches price only
                    ));

    assertFalse(evaluator.evaluate(record));
  }

  @Test
  void shouldHandleEmptyArray() {
    Filter nestedFilter = new NestedFilter("items", List.of(new EqualsFilter("status", "ACTIVE")));

    FilterEvaluator<TestRecord> evaluator = registry.buildEvaluator(nestedFilter, context);

    TestRecord record = new TestRecord().put("items", Collections.emptyList());
    assertFalse(evaluator.evaluate(record));
  }

  @Test
  void shouldHandleNullArray() {
    Filter nestedFilter = new NestedFilter("items", List.of(new EqualsFilter("status", "ACTIVE")));

    FilterEvaluator<TestRecord> evaluator = registry.buildEvaluator(nestedFilter, context);

    TestRecord record = new TestRecord().put("items", null);
    assertFalse(evaluator.evaluate(record));
  }

  @Test
  void shouldHandleMissingArray() {
    Filter nestedFilter = new NestedFilter("items", List.of(new EqualsFilter("status", "ACTIVE")));

    FilterEvaluator<TestRecord> evaluator = registry.buildEvaluator(nestedFilter, context);

    TestRecord record = new TestRecord();// no items field
    assertFalse(evaluator.evaluate(record));
  }

  @Test
  void shouldHandleMultipleConditionsOnSameField() {
    Filter nestedFilter =
        new NestedFilter(
            "items",
            Arrays.asList(
                new GreaterThanFilter("price", 50.0), new LessThanFilter("price", 100.0)));

    FilterEvaluator<TestRecord> evaluator = registry.buildEvaluator(nestedFilter, context);

    TestRecord record =
        new TestRecord()
            .put(
                "items",
                Collections.singletonList(
                    new TestRecord().put("price", 75.0)// matches both conditions
                    ));

    assertTrue(evaluator.evaluate(record));
  }

  @Test
  void shouldThrowExceptionForUnsupportedFilter() {
    Filter filter = new MatchAllFilter();
    FilterBuilder filterBuilder = FilterBuilderRegistry.getFilterBuilder(NestedFilter.TYPE);
    assertThrows(IllegalArgumentException.class, () -> filterBuilder.toEvaluator(filter, context));
  }
}
