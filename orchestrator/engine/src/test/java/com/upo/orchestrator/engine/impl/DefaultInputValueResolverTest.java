/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.upo.orchestrator.engine.ResolvableValue;
import com.upo.orchestrator.engine.VariableContainer;
import com.upo.orchestrator.engine.impl.value.OptimizedMapResolvableValue;
import com.upo.orchestrator.engine.impl.value.StaticResolvableValue;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.utilities.json.path.JsonPath;

public class DefaultInputValueResolverTest {

  private DefaultInputValueResolver resolver;
  @Mock private ProcessInstance processInstance;
  @Mock private VariableContainer variableContainer;

  @BeforeEach
  void setUp() {
   //noinspection resource
    MockitoAnnotations.openMocks(this);
    resolver = new DefaultInputValueResolver();
    when(processInstance.getVariableContainer()).thenReturn(variableContainer);
  }

  @Test
  @DisplayName("Should handle null input")
  void testNullInput() {
    assertNull(resolver.resolve(null));
  }

  @Test
  @DisplayName("Should handle primitive values")
  void testPrimitiveValues() {
    ResolvableValue result = resolver.resolve(42);
    assertInstanceOf(StaticResolvableValue.class, result);
    assertEquals(42, (int) result.evaluate(processInstance));
  }

  @Test
  @DisplayName("Should handle string with variable reference")
  void testStringWithVariable() {
    JsonPath expectedPath = JsonPath.create("taskId.input.name");
    when(variableContainer.readVariable(expectedPath)).thenReturn("John");

    ResolvableValue result = resolver.resolve("Hello {{taskId.input.name}}!");
    assertFalse(result instanceof StaticResolvableValue);
    assertEquals("Hello John!", result.evaluate(processInstance));

    verify(variableContainer).readVariable(expectedPath);
  }

  @Test
  @DisplayName("Should handle string with complex expression")
  void testStringWithExpression() {
    JsonPath amountPath = JsonPath.create("taskId.input.amount");
    JsonPath taxPath = JsonPath.create("taskId.input.tax");

    when(variableContainer.readVariable(amountPath)).thenReturn(100);
    when(variableContainer.readVariable(taxPath)).thenReturn(0.2);

    ResolvableValue result =
        resolver.resolve(
            "Total: [[(int)({{taskId.input.amount}} * (1 + {{taskId.input.tax}}))]] USD");
    assertEquals("Total: 120 USD", result.evaluate(processInstance));

    verify(variableContainer).readVariable(amountPath);
    verify(variableContainer).readVariable(taxPath);
  }

  @Test
  @DisplayName("Should handle map with mixed static and dynamic values")
  void testMixedMap() {
    JsonPath amountPath = JsonPath.create("task.input.amount");
    when(variableContainer.readVariable(amountPath)).thenReturn(1000);

    Map<String, Object> input = new LinkedHashMap<>();
    input.put("name", "John");
    input.put("amount", "{{task.input.amount}}");
    input.put("calculated", "[[{{task.input.amount}} * 1.2d]]");

    ResolvableValue result = resolver.resolve(input);
    assertInstanceOf(OptimizedMapResolvableValue.class, result);

    @SuppressWarnings("unchecked")
    Map<String, Object> evaluated = (Map<String, Object>) result.evaluate(processInstance);
    assertEquals("John", evaluated.get("name"));
    assertEquals(1000, evaluated.get("amount"));
    assertEquals(1200.0, evaluated.get("calculated"));

   // Verify order is preserved
    Iterator<String> keys = evaluated.keySet().iterator();
    assertEquals("name", keys.next());
    assertEquals("amount", keys.next());
    assertEquals("calculated", keys.next());

    verify(variableContainer, times(2)).readVariable(amountPath);
  }

  @Test
  @DisplayName("Should handle nested structures")
  void testNestedStructures() {
    JsonPath amountPath = JsonPath.create("task.input.amount");
    JsonPath taxPath = JsonPath.create("task.input.tax");

    when(variableContainer.readVariable(amountPath)).thenReturn(1000);
    when(variableContainer.readVariable(taxPath)).thenReturn(0.2);

    Map<String, Object> input = new LinkedHashMap<>();
    input.put("customer", Map.of("name", "John", "age", 30));
    input.put(
        "order",
        new LinkedHashMap<String, Object>() {
          {
            put("amount", "{{task.input.amount}}");
            put("tax", "{{task.input.tax}}");
            put("total", "[[{{task.input.amount}} * (1 + {{task.input.tax}})]]");
          }
        });

    ResolvableValue result = resolver.resolve(input);

    @SuppressWarnings("unchecked")
    Map<String, Object> evaluated = (Map<String, Object>) result.evaluate(processInstance);

    @SuppressWarnings("unchecked")
    Map<String, Object> customer = (Map<String, Object>) evaluated.get("customer");
    assertEquals("John", customer.get("name"));
    assertEquals(30, customer.get("age"));

    @SuppressWarnings("unchecked")
    Map<String, Object> order = (Map<String, Object>) evaluated.get("order");
    assertEquals(1000, order.get("amount"));
    assertEquals(0.2, order.get("tax"));
    assertEquals(1200.0, order.get("total"));

    verify(variableContainer, times(2)).readVariable(amountPath);
    verify(variableContainer, times(2)).readVariable(taxPath);
  }

  @Test
  @DisplayName("Should handle variable resolution errors")
  void testVariableResolutionError() {
    JsonPath path = JsonPath.create("nonexistent");
    when(variableContainer.readVariable(path))
        .thenThrow(new RuntimeException("Variable not found"));

    ResolvableValue result = resolver.resolve("{{nonexistent}}");
    assertThrows(RuntimeException.class, () -> result.evaluate(processInstance));

    verify(variableContainer).readVariable(path);
  }

  @Test
  @DisplayName("Should handle multiple variable references in single expression")
  void testMultipleVariablesInExpression() {
    JsonPath amountPath = JsonPath.create("task.input.amount");
    JsonPath taxPath = JsonPath.create("task.input.tax");
    JsonPath discountPath = JsonPath.create("task.input.discount");

    when(variableContainer.readVariable(amountPath)).thenReturn(1000);
    when(variableContainer.readVariable(taxPath)).thenReturn(0.2);
    when(variableContainer.readVariable(discountPath)).thenReturn(50);

    String expression =
        "[[{{task.input.amount}} * (1 + {{task.input.tax}}) - {{task.input.discount}}]]";
    ResolvableValue result = resolver.resolve(expression);

    assertEquals("1150.0", result.evaluate(processInstance).toString());

    verify(variableContainer).readVariable(amountPath);
    verify(variableContainer).readVariable(taxPath);
    verify(variableContainer).readVariable(discountPath);
  }

  @Test
  @DisplayName("Should handle conditional expressions")
  void testConditionalExpression() {
    JsonPath amountPath = JsonPath.create("task.input.amount");
    JsonPath thresholdPath = JsonPath.create("task.config.threshold");

    when(variableContainer.readVariable(amountPath)).thenReturn(2000);
    when(variableContainer.readVariable(thresholdPath)).thenReturn(1000);

    String expression = "[[{{task.input.amount}} > {{task.config.threshold}} ? 'high' : 'low']]";
    ResolvableValue result = resolver.resolve(expression);

    assertEquals("high", result.evaluate(processInstance));

    verify(variableContainer).readVariable(amountPath);
    verify(variableContainer).readVariable(thresholdPath);
  }

  @Test
  @DisplayName("Should handle map with all dynamic values")
  void testAllDynamicMap() {
    JsonPath path1 = JsonPath.create("var1");
    JsonPath path2 = JsonPath.create("var2");
    JsonPath path3 = JsonPath.create("var3");

    when(variableContainer.readVariable(path1)).thenReturn("value1");
    when(variableContainer.readVariable(path2)).thenReturn("value2");
    when(variableContainer.readVariable(path3)).thenReturn("value3");

    Map<String, Object> input = new LinkedHashMap<>();
    input.put("key1", "{{var1}}");
    input.put("key2", "{{var2}}");
    input.put("key3", "{{var3}}");

    ResolvableValue result = resolver.resolve(input);

    @SuppressWarnings("unchecked")
    Map<String, Object> evaluated = (Map<String, Object>) result.evaluate(processInstance);

    assertEquals("value1", evaluated.get("key1"));
    assertEquals("value2", evaluated.get("key2"));
    assertEquals("value3", evaluated.get("key3"));

   // Verify order
    Iterator<String> keys = evaluated.keySet().iterator();
    assertEquals("key1", keys.next());
    assertEquals("key2", keys.next());
    assertEquals("key3", keys.next());
  }

  @Test
  @DisplayName("Should handle complex nested expressions")
  void testComplexNestedExpressions() {
    JsonPath valuePath = JsonPath.create("value");
    JsonPath conditionPath = JsonPath.create("condition");

    when(variableContainer.readVariable(valuePath)).thenReturn(100);
    when(variableContainer.readVariable(conditionPath)).thenReturn(true);

    String expression = "[[{{condition}} ? {{value}} * 2 : {{value}} / 2]]";
    ResolvableValue result = resolver.resolve(expression);

    assertEquals(200, (int) result.evaluate(processInstance));

    verify(variableContainer).readVariable(conditionPath);
    verify(variableContainer, times(2)).readVariable(valuePath);
  }

  @Test
  @DisplayName("Should handle map with mixed expressions and variable references")
  void testMapWithMixedExpressionsAndVariables() {
    JsonPath amountPath = JsonPath.create("amount");
    JsonPath ratePath = JsonPath.create("rate");
    JsonPath statusPath = JsonPath.create("status");

    when(variableContainer.readVariable(amountPath)).thenReturn(1000);
    when(variableContainer.readVariable(ratePath)).thenReturn(0.1);
    when(variableContainer.readVariable(statusPath)).thenReturn("ACTIVE");

    Map<String, Object> input = new LinkedHashMap<>();
    input.put("baseAmount", "{{amount}}");
    input.put("calculatedFee", "[[{{amount}} * {{rate}}]]");
    input.put("description", "Status: {{status}}");
    input.put("isLarge", "[[{{amount}} > 500 ? true : false]]");

    ResolvableValue result = resolver.resolve(input);

    @SuppressWarnings("unchecked")
    Map<String, Object> evaluated = (Map<String, Object>) result.evaluate(processInstance);

    assertEquals(1000, evaluated.get("baseAmount"));
    assertEquals(100.0, evaluated.get("calculatedFee"));
    assertEquals("Status: ACTIVE", evaluated.get("description"));
    assertEquals(true, evaluated.get("isLarge"));
  }

  @Test
  @DisplayName("Should handle malformed variable references")
  void testMalformedVariableReferences() {
   // Empty braces should be treated as static text
    ResolvableValue result1 = resolver.resolve("{{}}");
    assertTrue(result1 instanceof StaticResolvableValue);
    assertEquals("{{}}", result1.evaluate(processInstance));

    assertThrows(IllegalArgumentException.class, () -> resolver.resolve("{{invalid..path}}"));
  }

  @Test
  @DisplayName("Should handle malformed expressions")
  void testMalformedExpressions() {
   // Unclosed variable reference
    ResolvableValue result1 = resolver.resolve("{{abc");
    assertEquals("{{abc", result1.evaluate(processInstance));

   // Unclosed expression
    ResolvableValue result2 = resolver.resolve("[[abc");
    assertEquals("[[abc", result2.evaluate(processInstance));
  }

  @Test
  @DisplayName("Should handle variable resolution returning null")
  void testNullVariableResolution() {
    JsonPath path = JsonPath.create("nullValue");
    when(variableContainer.readVariable(path)).thenReturn(null);

    ResolvableValue result = resolver.resolve("Value: {{nullValue}}");
    assertEquals("Value: ", result.evaluate(processInstance));
  }

  @Test
  @DisplayName("Should handle deeply nested map structure")
  void testDeeplyNestedMap() {
    JsonPath valuePath = JsonPath.create("value");
    when(variableContainer.readVariable(valuePath)).thenReturn(42);

    Map<String, Object> level3 = new LinkedHashMap<>();
    level3.put("value", "{{value}}");

    Map<String, Object> level2 = new LinkedHashMap<>();
    level2.put("nested", level3);

    Map<String, Object> level1 = new LinkedHashMap<>();
    level1.put("data", level2);

    ResolvableValue result = resolver.resolve(level1);

    @SuppressWarnings("unchecked")
    Map<String, Object> evaluated = (Map<String, Object>) result.evaluate(processInstance);

    assertEquals(42, ((Map) ((Map) evaluated.get("data")).get("nested")).get("value"));
  }
}
