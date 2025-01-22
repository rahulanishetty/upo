/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.upo.orchestrator.engine.ResolvableValue;
import com.upo.orchestrator.engine.Variable;
import com.upo.utilities.ds.Pair;

public class DefaultInputValueResolverDependenciesTest {

  private DefaultInputValueResolver resolver;

  @BeforeEach
  void setUp() {
    resolver = DefaultInputValueResolver.getInstance();
  }

  @Test
  void testStaticValuesDependencies() {
   // Test various static values
    Object[] inputs = {42, true, "static text", Collections.emptyMap(), Collections.emptyList()};

    for (Object input : inputs) {
      ResolvableValue value = resolver.resolve(input);
      assertTrue(
          value.getVariableDependencies().isEmpty(),
          "Static value should have no dependencies: " + input);
    }
  }

  @Test
  void testSimpleVariableReferences() {
   // Test simple variable reference strings
    Map<String, Set<Pair<String, Variable.Type>>> testCases = new HashMap<>();

   // Single references
    testCases.put("{{ task1.input.name }}", Set.of(Pair.of("task1", Variable.Type.INPUT)));
    testCases.put("{{ task2.output.status }}", Set.of(Pair.of("task2", Variable.Type.OUTPUT)));

   // Multiple references in one string
    testCases.put(
        "Name: {{ task1.input.name }}, Age: {{ task1.input.age }}",
        Set.of(Pair.of("task1", Variable.Type.INPUT)));
    testCases.put(
        "User: {{ task1.input.name }}, Status: {{ task2.output.verified }}",
        Set.of(Pair.of("task1", Variable.Type.INPUT), Pair.of("task2", Variable.Type.OUTPUT)));

    for (Map.Entry<String, Set<Pair<String, Variable.Type>>> entry : testCases.entrySet()) {
      ResolvableValue value = resolver.resolve(entry.getKey());
      assertEquals(
          entry.getValue(),
          value.getVariableDependencies(),
          "Dependencies mismatch for: " + entry.getKey());
    }
  }

  @Test
  void testComplexMapStructure() {
    Map<String, Object> input = new LinkedHashMap<>();
    input.put("userId", "{{ task1.input.id }}");
    input.put("userName", "{{ task1.input.name }}");
    input.put("verified", "{{ task2.output.isVerified }}");
    input.put("staticField", "constant");
    input.put(
        "nestedMap",
        Map.of(
            "address", "{{ task1.input.address }}",
            "status", "{{ task2.output.status }}"));

    ResolvableValue value = resolver.resolve(input);
    Set<Pair<String, Variable.Type>> deps = value.getVariableDependencies();

    assertEquals(2, deps.size());
    assertTrue(deps.contains(Pair.of("task1", Variable.Type.INPUT)));
    assertTrue(deps.contains(Pair.of("task2", Variable.Type.OUTPUT)));
  }

  @Test
  void testArrayTransformer() {
    Map<String, Object> input = new LinkedHashMap<>();
    input.put("__@type", "arrayTransformer");
    input.put("source", "{{ task1.input.users }}");
    input.put(
        "item",
        Map.of(
            "id", "{{ item.userId }}",
            "name", "{{ item.fullName }}",
            "status", "{{ task2.output.status }}"));

    ResolvableValue value = resolver.resolve(input);
    Set<Pair<String, Variable.Type>> deps = value.getVariableDependencies();

    assertEquals(2, deps.size());
    assertTrue(deps.contains(Pair.of("task1", Variable.Type.INPUT)));
    assertTrue(deps.contains(Pair.of("task2", Variable.Type.OUTPUT)));
  }

  @Test
  void testNestedListStructure() {
    List<Object> input =
        Arrays.asList(
            "{{ task1.input.name }}",
            42,
            Map.of("status", "{{ task2.output.status }}"),
            Arrays.asList(
                "{{ task3.input.value }}", "static", Map.of("nested", "{{ task1.input.data }}")));

    ResolvableValue value = resolver.resolve(input);
    Set<Pair<String, Variable.Type>> deps = value.getVariableDependencies();

    assertEquals(3, deps.size());
    assertTrue(deps.contains(Pair.of("task1", Variable.Type.INPUT)));
    assertTrue(deps.contains(Pair.of("task2", Variable.Type.OUTPUT)));
    assertTrue(deps.contains(Pair.of("task3", Variable.Type.INPUT)));
  }

  @Test
  void testExpressionDependencies() {
    String[] expressions = {
      "{{ task1.input.age }} > 18",
      "{{ task1.input.score }} >= 75 && {{ task2.output.verified }}",
      "{{ task1.input.type }} == 'premium' ? {{ task2.output.price }} * 0.9 : {{ task2.output.price }}"
    };

    for (String expr : expressions) {
      ResolvableValue value = resolver.resolve(expr);
      Set<Pair<String, Variable.Type>> deps = value.getVariableDependencies();

      assertFalse(deps.isEmpty(), "Expression should have dependencies: " + expr);
      deps.forEach(
          dep ->
              assertTrue(
                  dep.getFirstElement().startsWith("task"),
                  "Task ID should start with 'task': " + dep.getFirstElement()));
    }
  }

  @Test
  void testComplexProcessDefinition() {
   // Simulating a complex process task definition
    Map<String, Object> taskDef = new LinkedHashMap<>();
    taskDef.put("taskId", "processUser");
    taskDef.put("type", "userProcessor");
    taskDef.put(
        "input",
        Map.of(
            "users",
            "{{ task1.input.userList }}",
            "config",
            Map.of(
                "processType",
                "{{ task2.output.type }}",
                "settings",
                Arrays.asList("{{ task3.input.setting1 }}", "{{ task3.input.setting2 }}"))));
    taskDef.put(
        "transform",
        Map.of(
            "__@type", "arrayTransformer",
            "source", "{{ task1.input.userList }}",
            "item",
                Map.of(
                    "id", "{{ item.id }}",
                    "name", "{{ item.name }}",
                    "status", "{{ task2.output.status }}",
                    "settings", "{{ task3.input.userSettings }}")));

    ResolvableValue value = resolver.resolve(taskDef);
    Set<Pair<String, Variable.Type>> deps = value.getVariableDependencies();

    assertEquals(3, deps.size());
    assertTrue(deps.contains(Pair.of("task1", Variable.Type.INPUT)));
    assertTrue(deps.contains(Pair.of("task2", Variable.Type.OUTPUT)));
    assertTrue(deps.contains(Pair.of("task3", Variable.Type.INPUT)));
  }
}
