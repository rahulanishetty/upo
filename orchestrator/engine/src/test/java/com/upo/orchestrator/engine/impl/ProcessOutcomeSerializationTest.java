/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.alibaba.fastjson2.JSON;
import com.upo.orchestrator.engine.ProcessOutcome;

public class ProcessOutcomeSerializationTest {

  @Test
  void testSuccessOutcomeSerialization() {
   // Create success outcome with simple result
    Map<String, Object> result = Map.of("key", "value", "count", 42);
    ProcessOutcome outcome = new ProcessOutcome.Success(result);

   // Serialize
    String json = JSON.toJSONString(outcome);

   // Verify JSON structure
    assertTrue(json.contains("\"type\":\"SUCCESS\""));
    assertTrue(json.contains("\"result\":{"));
    assertTrue(json.contains("\"key\":\"value\""));
    assertTrue(json.contains("\"count\":42"));

   // Deserialize and verify
    ProcessOutcome deserialized = JSON.parseObject(json, ProcessOutcome.class);
    assertInstanceOf(ProcessOutcome.Success.class, deserialized);
    ProcessOutcome.Success success = (ProcessOutcome.Success) deserialized;
    assertEquals("SUCCESS", success.getType());

    @SuppressWarnings("unchecked")
    Map<String, Object> deserializedResult = (Map<String, Object>) success.getResult();
    assertEquals("value", deserializedResult.get("key"));
    assertEquals(42, deserializedResult.get("count"));
  }

  @Test
  void testFailureOutcomeSerialization() {
   // Create failure outcome with error details
    Map<String, Object> error =
        Map.of(
            "code", "ERR_001",
            "message", "Process failed",
            "details", Map.of("step", "validation"));
    ProcessOutcome outcome = new ProcessOutcome.Failure(error);

   // Serialize
    String json = JSON.toJSONString(outcome);

   // Verify JSON structure
    assertTrue(json.contains("\"type\":\"FAILURE\""));
    assertTrue(json.contains("\"failure\":{"));
    assertTrue(json.contains("\"code\":\"ERR_001\""));
    assertTrue(json.contains("\"message\":\"Process failed\""));

   // Deserialize and verify
    ProcessOutcome deserialized = JSON.parseObject(json, ProcessOutcome.class);
    assertInstanceOf(ProcessOutcome.Failure.class, deserialized);
    ProcessOutcome.Failure failure = (ProcessOutcome.Failure) deserialized;
    assertEquals("FAILURE", failure.getType());

    @SuppressWarnings("unchecked")
    Map<String, Object> deserializedError = (Map<String, Object>) failure.getFailure();
    assertEquals("ERR_001", deserializedError.get("code"));
    assertEquals("Process failed", deserializedError.get("message"));

    @SuppressWarnings("unchecked")
    Map<String, String> details = (Map<String, String>) deserializedError.get("details");
    assertEquals("validation", details.get("step"));
  }

  @Test
  void testComplexResultSerialization() {
   // Test with complex nested result
    Map<String, Object> complexResult =
        Map.of(
            "list", List.of(1, 2, 3),
            "nested", Map.of("inner", Map.of("value", "test")));
    ProcessOutcome outcome = new ProcessOutcome.Success(complexResult);

   // Serialize and deserialize
    String json = JSON.toJSONString(outcome);
    ProcessOutcome deserialized = JSON.parseObject(json, ProcessOutcome.class);

    assertInstanceOf(ProcessOutcome.Success.class, deserialized);
    ProcessOutcome.Success success = (ProcessOutcome.Success) deserialized;

    @SuppressWarnings("unchecked")
    Map<String, Object> deserializedResult = (Map<String, Object>) success.getResult();

    @SuppressWarnings("unchecked")
    List<Integer> list = (List<Integer>) deserializedResult.get("list");
    assertEquals(List.of(1, 2, 3), list);

    @SuppressWarnings("unchecked")
    Map<String, Object> nested = (Map<String, Object>) deserializedResult.get("nested");
    @SuppressWarnings("unchecked")
    Map<String, String> inner = (Map<String, String>) nested.get("inner");
    assertEquals("test", inner.get("value"));
  }

  @Test
  void testNullValuesSerialization() {
   // Test with null result/failure
    ProcessOutcome success = new ProcessOutcome.Success(null);
    ProcessOutcome failure = new ProcessOutcome.Failure(null);

   // Serialize and deserialize success
    String successJson = JSON.toJSONString(success);
    ProcessOutcome deserializedSuccess = JSON.parseObject(successJson, ProcessOutcome.class);
    assertInstanceOf(ProcessOutcome.Success.class, deserializedSuccess);
    assertNull(((ProcessOutcome.Success) deserializedSuccess).getResult());

   // Serialize and deserialize failure
    String failureJson = JSON.toJSONString(failure);
    ProcessOutcome deserializedFailure = JSON.parseObject(failureJson, ProcessOutcome.class);
    assertInstanceOf(ProcessOutcome.Failure.class, deserializedFailure);
    assertNull(((ProcessOutcome.Failure) deserializedFailure).getFailure());
  }

  @Test
  void testInvalidTypeSerialization() {
    String invalidJson = "{\"type\":\"INVALID\"}";
    assertThrows(Exception.class, () -> JSON.parseObject(invalidJson, ProcessOutcome.class));
  }
}
