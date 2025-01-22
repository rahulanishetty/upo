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
import com.upo.orchestrator.engine.impl.value.ArrayToMapTransformResolver;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.utilities.json.path.JsonPath;

public class ArrayToMapTransformResolverTest {
  private InputValueResolver resolver;
  @Mock private ProcessInstance processInstance;
  @Mock private VariableContainer variableContainer;

  @BeforeEach
  void setUp() {
   //noinspection resource
    MockitoAnnotations.openMocks(this);
    resolver = DefaultInputValueResolver.getInstance();
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
    assertNull(new ArrayToMapTransformResolver(resolver).resolve(null));
  }

  @Test
  @DisplayName("Should transform array to map with simple key-value")
  void testSimpleKeyValueTransform() {
    List<Map<String, Object>> sourceData =
        Arrays.asList(Map.of("id", "user1", "name", "John"), Map.of("id", "user2", "name", "Jane"));

    JsonPath sourcePath = JsonPath.create("task.input.users");
    when(variableContainer.readVariable(sourcePath)).thenReturn(sourceData);

    Map<String, Object> transformDef = new LinkedHashMap<>();
    transformDef.put("__@type", "arrayToMapTransformer");
    transformDef.put("source", "{{task.input.users}}");
    transformDef.put("key", "{{item.id}}");
    transformDef.put("value", "{{item.name}}");

    Object result = resolver.resolve(transformDef).evaluate(processInstance);

    assertInstanceOf(Map.class, result);
    Map<?, ?> resultMap = (Map<?, ?>) result;
    assertEquals(2, resultMap.size());
    assertEquals("John", resultMap.get("user1"));
    assertEquals("Jane", resultMap.get("user2"));
  }

  @Test
  @DisplayName("Should transform array to map with complex value")
  void testComplexValueTransform() {
    List<Map<String, Object>> sourceData =
        Arrays.asList(
            Map.of("id", 1, "name", "John", "age", 30), Map.of("id", 2, "name", "Jane", "age", 25));

    JsonPath sourcePath = JsonPath.create("task.input.users");
    when(variableContainer.readVariable(sourcePath)).thenReturn(sourceData);

    Map<String, Object> transformDef = new LinkedHashMap<>();
    transformDef.put("__@type", "arrayToMapTransformer");
    transformDef.put("source", "{{task.input.users}}");
    transformDef.put("key", "{{item.id}}");
    transformDef.put(
        "value",
        Map.of(
            "fullName", "{{item.name}}",
            "ageNext", "[[{{item.age}} + 1]]"));

    Object result = resolver.resolve(transformDef).evaluate(processInstance);

    Map<?, ?> resultMap = (Map<?, ?>) result;
    assertEquals(2, resultMap.size());

    Map<?, ?> user1 = (Map<?, ?>) resultMap.get(1);
    assertEquals("John", user1.get("fullName"));
    assertEquals(31, user1.get("ageNext"));

    Map<?, ?> user2 = (Map<?, ?>) resultMap.get(2);
    assertEquals("Jane", user2.get("fullName"));
    assertEquals(26, user2.get("ageNext"));
  }

  @Test
  @DisplayName("Should handle duplicate keys by using last value")
  void testDuplicateKeys() {
    List<Map<String, Object>> sourceData =
        Arrays.asList(
            Map.of("type", "user", "value", "first"), Map.of("type", "user", "value", "second"));

    JsonPath sourcePath = JsonPath.create("task.input.data");
    when(variableContainer.readVariable(sourcePath)).thenReturn(sourceData);

    Map<String, Object> transformDef = new LinkedHashMap<>();
    transformDef.put("__@type", "arrayToMapTransformer");
    transformDef.put("source", "{{task.input.data}}");
    transformDef.put("key", "{{item.type}}");
    transformDef.put("value", "{{item.value}}");

    Object result = resolver.resolve(transformDef).evaluate(processInstance);

    Map<?, ?> resultMap = (Map<?, ?>) result;
    assertEquals(1, resultMap.size());
    assertEquals("second", resultMap.get("user"));
  }

  @Test
  @DisplayName("Should skip null keys")
  void testNullKeys() {
    List<Map<String, Object>> sourceData =
        Arrays.asList(
            Map.of("id", "valid", "value", "keep"),
            new LinkedHashMap<>() {
              {
                put("id", null);
                put("value", "skip");
              }
            });

    JsonPath sourcePath = JsonPath.create("task.input.data");
    when(variableContainer.readVariable(sourcePath)).thenReturn(sourceData);

    Map<String, Object> transformDef = new LinkedHashMap<>();
    transformDef.put("__@type", "arrayToMapTransformer");
    transformDef.put("source", "{{task.input.data}}");
    transformDef.put("key", "{{item.id}}");
    transformDef.put("value", "{{item.value}}");

    Object result = resolver.resolve(transformDef).evaluate(processInstance);

    Map<?, ?> resultMap = (Map<?, ?>) result;
    assertEquals(1, resultMap.size());
    assertEquals("keep", resultMap.get("valid"));
  }

  @Test
  @DisplayName("Should use default source with null when source not specified - Map Transform")
  void testDefaultSourceMapTransform() {
    Map<String, Object> transformDef = new LinkedHashMap<>();
    transformDef.put("__@type", "arrayToMapTransformer");
    transformDef.put("key", "defaultKey");
    transformDef.put("value", "defaultValue");

    Object result = resolver.resolve(transformDef).evaluate(processInstance);

    assertInstanceOf(Map.class, result);
    Map<?, ?> resultMap = (Map<?, ?>) result;
    assertEquals(1, resultMap.size());
    assertEquals("defaultValue", resultMap.get("defaultKey"));
  }

  @Test
  @DisplayName("Should use default source with null when source is explicitly null - Map Transform")
  void testNullSourceMapTransform() {
    Map<String, Object> transformDef = new LinkedHashMap<>();
    transformDef.put("__@type", "arrayToMapTransformer");
    transformDef.put("source", null);
    transformDef.put("key", "key1");
    transformDef.put("value", "value1");

    Object result = resolver.resolve(transformDef).evaluate(processInstance);

    assertInstanceOf(Map.class, result);
    Map<?, ?> resultMap = (Map<?, ?>) result;
    assertEquals(1, resultMap.size());
    assertEquals("value1", resultMap.get("key1"));
  }

  @Test
  @DisplayName("Should handle null key with default source - Map Transform")
  void testNullKeyWithDefaultSourceMapTransform() {
    Map<String, Object> transformDef = new LinkedHashMap<>();
    transformDef.put("__@type", "arrayToMapTransformer");
    transformDef.put("key", null);
    transformDef.put("value", "value");
    assertNull(resolver.resolve(transformDef));
  }
}
