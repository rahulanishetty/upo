/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.upo.orchestrator.engine.ResolvableValue;
import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.InputValueResolver;
import com.upo.utilities.ds.Pair;
import com.upo.utilities.filter.api.*;
import com.upo.utilities.filter.impl.FilterEvaluator;

public class ProcessFilterEvaluationTest {
  private TestInputValueResolver valueResolver;
  private ProcessInstance processInstance;

  @BeforeEach
  void setUp() {
    valueResolver = new TestInputValueResolver();
    processInstance = mock(ProcessInstance.class);
  }

  @Nested
  class ComparisonFilterTests {

    @Test
    void testEqualsFilter() {
     // Setup task variables
      valueResolver.addVariable("task1.output.value", 42);
      valueResolver.addVariable("task2.input.threshold", 42);

      Filter filter = createFilter("EQUALS", "task1.output.value", "task2.input.threshold");
      FilterEvaluator<ProcessInstance> evaluator =
          ProcessFilterEvaluatorFactory.createEvaluator(filter, valueResolver);

      assertTrue(evaluator.evaluate(processInstance));
    }

    @Test
    void testGreaterThanFilter() {
      valueResolver.addVariable("task1.output.count", 100);
      valueResolver.addVariable("task2.input.limit", 50);

      Filter filter = createFilter("GT", "task1.output.count", "task2.input.limit");
      FilterEvaluator<ProcessInstance> evaluator =
          ProcessFilterEvaluatorFactory.createEvaluator(filter, valueResolver);

      assertTrue(evaluator.evaluate(processInstance));
    }
  }

  @Nested
  class CollectionFilterTests {

    @Test
    void testContainsFilter() {
      valueResolver.addVariable("task1.output.text", "Hello World");
      valueResolver.addVariable("task2.input.search", "World");

      Filter filter = createFilter("CONTAINS", "task1.output.text", "task2.input.search");
      FilterEvaluator<ProcessInstance> evaluator =
          ProcessFilterEvaluatorFactory.createEvaluator(filter, valueResolver);

      assertTrue(evaluator.evaluate(processInstance));
    }
  }

  @Nested
  class PatternMatchingTests {

    @Test
    void testRegexFilter() {
      valueResolver.addVariable("task1.output.code", "ABC-123");
      valueResolver.addVariable("task2.input.pattern", "^[A-Z]+-\\d+$");

      Filter filter = createFilter("REGEX", "task1.output.code", "task2.input.pattern");
      FilterEvaluator<ProcessInstance> evaluator =
          ProcessFilterEvaluatorFactory.createEvaluator(filter, valueResolver);

      assertTrue(evaluator.evaluate(processInstance));
    }

    @Test
    void testIRegexFilter() {
      valueResolver.addVariable("task1.output.text", "Hello");
      valueResolver.addVariable("task2.input.pattern", "^hello$");

      Filter filter = createFilter("IREGEX", "task1.output.text", "task2.input.pattern");
      FilterEvaluator<ProcessInstance> evaluator =
          ProcessFilterEvaluatorFactory.createEvaluator(filter, valueResolver);

      assertTrue(evaluator.evaluate(processInstance));
    }
  }

  @Nested
  class LogicalOperatorTests {

    @Test
    void testAndFilter() {
      valueResolver.addVariable("task1.output.age", 25);
      valueResolver.addVariable("task2.input.min_age", 18);
      valueResolver.addVariable("task2.input.max_age", 30);

      Filter ageAboveMin = createFilter("GT", "task1.output.age", "task2.input.min_age");
      Filter ageBelowMax = createFilter("LT", "task1.output.age", "task2.input.max_age");
      Filter andFilter = createAndFilter(Arrays.asList(ageAboveMin, ageBelowMax));

      FilterEvaluator<ProcessInstance> evaluator =
          ProcessFilterEvaluatorFactory.createEvaluator(andFilter, valueResolver);

      assertTrue(evaluator.evaluate(processInstance));
    }

    @Test
    void testOrFilter() {
      valueResolver.addVariable("task1.output.status", "PENDING");
      valueResolver.addVariable("task2.input.status1", "PENDING");
      valueResolver.addVariable("task2.input.status2", "IN_PROGRESS");

      Filter status1Match = createFilter("EQUALS", "task1.output.status", "task2.input.status1");
      Filter status2Match = createFilter("EQUALS", "task1.output.status", "task2.input.status2");
      Filter orFilter = createOrFilter(Arrays.asList(status1Match, status2Match));

      FilterEvaluator<ProcessInstance> evaluator =
          ProcessFilterEvaluatorFactory.createEvaluator(orFilter, valueResolver);

      assertTrue(evaluator.evaluate(processInstance));
    }
  }

 // Test helper classes and methods

  private static class TestInputValueResolver implements InputValueResolver {
    private final Map<String, Object> variables = new HashMap<>();

    void addVariable(String path, Object value) {
      variables.put(path, value);
    }

    @Override
    public ResolvableValue resolve(Object input) {
      if (input instanceof String path && variables.containsKey(path)) {
        return new TestResolvableValue(variables.get(path));
      }
      return new TestResolvableValue(input);
    }
  }

  private static class TestResolvableValue implements ResolvableValue {
    private final Object value;

    TestResolvableValue(Object value) {
      this.value = value;
    }

    @Override
    public <T> T evaluate(ProcessInstance instance) {
     //noinspection unchecked
      return (T) value;
    }

    @Override
    public Set<Pair<String, Variable.Type>> getVariableDependencies() {
      return Collections.emptySet();// Simplified for testing
    }
  }

 // Helper methods to create filters
  private Filter createFilter(String type, String field, String value) {
    return switch (type) {
      case "EQUALS" -> new EqualsFilter(field, value);
      case "GT" -> new GreaterThanFilter(field, value);
      case "LT" -> new LessThanFilter(field, value);
      case "IN" -> new InFilter(field, Collections.singletonList(value));
      case "CONTAINS" -> new ContainsFilter(field, value);
      case "REGEX" -> new RegexFilter(field, value);
      case "IREGEX" -> new InsensitiveRegexFilter(field, value);
      default -> throw new IllegalArgumentException("unsupported type:" + type);
    };
  }

  private Filter createAndFilter(List<Filter> filters) {
    return new AndFilter(filters);
  }

  private Filter createOrFilter(List<Filter> filters) {
    return new OrFilter(filters);
  }
}
