/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.alibaba.fastjson2.JSON;
import com.upo.orchestrator.engine.ProcessFlowStatus;
import com.upo.orchestrator.engine.Signal;

public class SignalSerializationTest {

  @Test
  void testResumeSignalSerialization() {
   // Create signal with simple data
    Map<String, String> callbackData = Map.of("key", "value");
    Signal.Resume signal = Signal.Resume.with(callbackData);

   // Serialize
    String json = JSON.toJSONString(signal);

   // Verify JSON structure
    assertTrue(json.contains("\"type\":\"resume\""));
    assertTrue(json.contains("\"flowStatus\":\"CONTINUE\""));
    assertTrue(json.contains("\"callbackData\":{\"key\":\"value\"}"));

   // Deserialize and verify
    Signal deserialized = JSON.parseObject(json, Signal.class);
    assertInstanceOf(Signal.Resume.class, deserialized);
    Signal.Resume typedSignal = (Signal.Resume) deserialized;
    assertEquals("resume", typedSignal.getType());
    assertEquals(ProcessFlowStatus.CONTINUE, typedSignal.getFlowStatus());
    @SuppressWarnings("unchecked")
    Map<String, String> deserializedData = (Map<String, String>) typedSignal.getCallbackData();
    assertEquals("value", deserializedData.get("key"));
  }

  @Test
  void testStopSignalFailedSerialization() {
   // Create failed signal
    Map<String, String> errorData = Map.of("error", "Test error");
    Signal.Stop signal = Signal.Stop.failed(errorData);

   // Serialize
    String json = JSON.toJSONString(signal);

   // Verify JSON structure
    assertTrue(json.contains("\"type\":\"stop\""));
    assertTrue(json.contains("\"flowStatus\":\"FAILED\""));
    assertTrue(json.contains("\"callbackData\":{\"error\":\"Test error\"}"));

   // Deserialize and verify
    Signal deserialized = JSON.parseObject(json, Signal.class);
    assertInstanceOf(Signal.Stop.class, deserialized);
    Signal.Stop typedSignal = (Signal.Stop) deserialized;
    assertEquals("stop", typedSignal.getType());
    assertEquals(ProcessFlowStatus.FAILED, typedSignal.getFlowStatus());
    @SuppressWarnings("unchecked")
    Map<String, String> deserializedData = (Map<String, String>) typedSignal.getCallbackData();
    assertEquals("Test error", deserializedData.get("error"));
  }

  @Test
  void testStopSignalSuspendedSerialization() {
   // Create suspended signal
    Signal.Stop signal = Signal.Stop.suspended();

   // Serialize
    String json = JSON.toJSONString(signal);

   // Verify JSON structure
    assertTrue(json.contains("\"type\":\"stop\""));
    assertTrue(json.contains("\"flowStatus\":\"SUSPENDED\""));

   // Deserialize and verify
    Signal deserialized = JSON.parseObject(json, Signal.class);
    assertInstanceOf(Signal.Stop.class, deserialized);
    Signal.Stop typedSignal = (Signal.Stop) deserialized;
    assertEquals("stop", typedSignal.getType());
    assertEquals(ProcessFlowStatus.SUSPENDED, typedSignal.getFlowStatus());
    assertNull(typedSignal.getCallbackData());
  }

  @Test
  void testReturnSignalSerialization() {
   // Create return signal with complex value
    Map<String, Object> returnValue =
        new LinkedHashMap<>() {
          {
            put("id", 123);
            put("data", Map.of("nested", "value"));
          }
        };
    Signal.Return signal = Signal.Return.with(returnValue);

   // Serialize
    String json = JSON.toJSONString(signal);

   // Verify JSON structure
    assertTrue(json.contains("\"type\":\"return\""));
    assertTrue(json.contains("\"flowStatus\":\"COMPLETED\""));
    assertTrue(json.contains("\"returnValue\":{\"id\":123"));

   // Deserialize and verify
    Signal deserialized = JSON.parseObject(json, Signal.class);
    assertInstanceOf(Signal.Return.class, deserialized);
    Signal.Return typedSignal = (Signal.Return) deserialized;
    assertEquals("return", typedSignal.getType());
    assertEquals(ProcessFlowStatus.COMPLETED, typedSignal.getFlowStatus());
    @SuppressWarnings("unchecked")
    Map<String, Object> deserializedValue = (Map<String, Object>) typedSignal.getReturnValue();
    assertEquals(123, deserializedValue.get("id"));
    @SuppressWarnings("unchecked")
    Map<String, String> nestedData = (Map<String, String>) deserializedValue.get("data");
    assertEquals("value", nestedData.get("nested"));
  }

  @Test
  void testComplexObjectSerialization() {
   // Test with complex object containing lists and nested maps
    Map<String, Object> complexData =
        Map.of(
            "list", List.of(1, 2, 3),
            "nested", Map.of("inner", Map.of("value", "test")));
    Signal.Resume signal = Signal.Resume.with(complexData);

   // Serialize and deserialize
    String json = JSON.toJSONString(signal);
    Signal deserialized = JSON.parseObject(json, Signal.class);

    assertInstanceOf(Signal.Resume.class, deserialized);
    @SuppressWarnings("unchecked")
    Map<String, Object> deserializedData =
        (Map<String, Object>) ((Signal.Resume) deserialized).getCallbackData();

    @SuppressWarnings("unchecked")
    List<Integer> list = (List<Integer>) deserializedData.get("list");
    assertEquals(List.of(1, 2, 3), list);

    @SuppressWarnings("unchecked")
    Map<String, Object> nested = (Map<String, Object>) deserializedData.get("nested");
    @SuppressWarnings("unchecked")
    Map<String, String> inner = (Map<String, String>) nested.get("inner");
    assertEquals("test", inner.get("value"));
  }

  @Test
  void testInvalidTypeSerialization() {
   // Create invalid JSON missing required fields
    String invalidJson = "{\"type\":\"invalid\"}";

   // Verify it throws exception
    assertThrows(Exception.class, () -> JSON.parseObject(invalidJson, Signal.class));
  }
}
