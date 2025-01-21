/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.upo.orchestrator.engine.InputValueResolver;
import com.upo.orchestrator.engine.VariableContainer;
import com.upo.orchestrator.engine.impl.value.ArrayTransformResolver;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.utilities.json.path.JsonPath;

public class ArrayTransformResolverTest {
  private InputValueResolver resolver;
  @Mock private ProcessInstance processInstance;
  @Mock private VariableContainer variableContainer;

  @BeforeEach
  void setUp() {
   //noinspection resource
    MockitoAnnotations.openMocks(this);
    resolver = new DefaultInputValueResolver();
   // Create a holder for the current variable container
    AtomicReference<VariableContainer> currentContainer = new AtomicReference<>(variableContainer);

   // Setup processInstance to track variable container changes
    when(processInstance.getVariableContainer()).thenAnswer(inv -> currentContainer.get());
    doAnswer(
            invocation -> {
              currentContainer.set(invocation.getArgument(0));
              return null;
            })
        .when(processInstance)
        .setVariableContainer(any());

   // Initialize with the base variable container
    processInstance.setVariableContainer(variableContainer);
  }

  @Test
  @DisplayName("Should handle null input")
  void testNullInput() {
    assertNull(new ArrayTransformResolver(resolver).resolve(null));
  }

  @Test
  @DisplayName("Should transform array with simple mapping")
  void testSimpleTransform() {
   // Setup source data
    List<Map<String, Object>> sourceData =
        Arrays.asList(Map.of("id", 1, "name", "John"), Map.of("id", 2, "name", "Jane"));

    JsonPath sourcePath = JsonPath.create("task.input.users");
    when(variableContainer.readVariable(sourcePath)).thenReturn(sourceData);

   // Create transform definition
    Map<String, Object> transformDef = new LinkedHashMap<>();
    transformDef.put("__@type", "arrayTransformer");
    transformDef.put("source", "{{task.input.users}}");
    transformDef.put(
        "item",
        Map.of(
            "userId", "{{item.id}}",
            "userName", "{{item.name}}"));

   // Execute transform
    Object result = resolver.resolve(transformDef).evaluate(processInstance);

   // Verify result
    assertInstanceOf(List.class, result);
    List<?> resultList = (List<?>) result;
    assertEquals(2, resultList.size());

    Map<?, ?> firstItem = (Map<?, ?>) resultList.getFirst();
    assertEquals(1, firstItem.get("userId"));
    assertEquals("John", firstItem.get("userName"));

    Map<?, ?> secondItem = (Map<?, ?>) resultList.get(1);
    assertEquals(2, secondItem.get("userId"));
    assertEquals("Jane", secondItem.get("userName"));
  }

  @Test
  @DisplayName("Should handle empty source array")
  void testEmptySourceArray() {
    JsonPath sourcePath = JsonPath.create("task.input.empty");
    when(variableContainer.readVariable(sourcePath)).thenReturn(List.of());

    Map<String, Object> transformDef = new LinkedHashMap<>();
    transformDef.put("__@type", "arrayTransformer");
    transformDef.put("source", "{{task.input.empty}}");
    transformDef.put("item", Map.of("field", "{{item}}"));

    Object result = resolver.resolve(transformDef).evaluate(processInstance);
    assertInstanceOf(List.class, result);
    assertEquals(0, ((List<?>) result).size());
  }

  @Test
  @DisplayName("Should transform with expression")
  void testTransformWithExpression() {
    List<Map<String, Object>> sourceData =
        Arrays.asList(Map.of("amount", 100, "tax", 0.2), Map.of("amount", 200, "tax", 0.1));

    JsonPath sourcePath = JsonPath.create("task.input.orders");
    when(variableContainer.readVariable(sourcePath)).thenReturn(sourceData);

    Map<String, Object> transformDef = new LinkedHashMap<>();
    transformDef.put("__@type", "arrayTransformer");
    transformDef.put("source", "{{task.input.orders}}");
    transformDef.put("item", Map.of("total", "[[(float)({{item.amount}} * (1 + {{item.tax}}))]]"));

    Object result = resolver.resolve(transformDef).evaluate(processInstance);
    List<?> resultList = (List<?>) result;

    Map<?, ?> firstItem = (Map<?, ?>) resultList.get(0);
    Map<?, ?> secondItem = (Map<?, ?>) resultList.get(1);

    assertEquals(120.0f, firstItem.get("total"));
    assertEquals(220.0f, secondItem.get("total"));
  }

  @Test
  @DisplayName("Should use default source with null when source not specified - Array Transform")
  void testDefaultSourceArrayTransform() {
   // Create transform definition without source
    Map<String, Object> transformDef = new LinkedHashMap<>();
    transformDef.put("__@type", "arrayTransformer");
    transformDef.put(
        "item",
        new LinkedHashMap<>() {
          {
            put("field1", "static");
            put("field2", 42);
          }
        });

    Object result = resolver.resolve(transformDef).evaluate(processInstance);

    assertInstanceOf(List.class, result);
    List<?> resultList = (List<?>) result;
    assertEquals(1, resultList.size());

    Map<?, ?> singleItem = (Map<?, ?>) resultList.getFirst();
    assertEquals("static", singleItem.get("field1"));
    assertEquals(42, singleItem.get("field2"));
  }

  @Test
  @DisplayName(
      "Should use default source with null when source is explicitly null - Array Transform")
  void testNullSourceArrayTransform() {
    Map<String, Object> transformDef = new LinkedHashMap<>();
    transformDef.put("__@type", "arrayTransformer");
    transformDef.put("source", null);
    transformDef.put("item", "static");

    Object result = resolver.resolve(transformDef).evaluate(processInstance);

    assertInstanceOf(List.class, result);
    List<?> resultList = (List<?>) result;
    assertEquals(1, resultList.size());
    assertEquals("static", resultList.getFirst());
  }

  @Test
  @DisplayName("Should handle null item template with default source - Array Transform")
  void testNullItemWithDefaultSourceArrayTransform() {
    Map<String, Object> transformDef = new LinkedHashMap<>();
    transformDef.put("__@type", "arrayTransformer");
    transformDef.put("item", null);

    assertNull(resolver.resolve(transformDef));
  }
}
