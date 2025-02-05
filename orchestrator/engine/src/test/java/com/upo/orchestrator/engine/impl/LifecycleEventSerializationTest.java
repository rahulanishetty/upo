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
import com.upo.orchestrator.engine.Signal;
import com.upo.orchestrator.engine.impl.events.LifecycleEvent;

public class LifecycleEventSerializationTest {

  @Test
  void testStartProcessSerialization() {
   // Create event
    LifecycleEvent.StartProcess event = new LifecycleEvent.StartProcess();
    event.setProcessDefinitionId("process-1");
    event.setPayload(Map.of("key", "value"));

   // Serialize
    String json = JSON.toJSONString(event);

   // Verify JSON structure
    assertTrue(json.contains("\"type\":\"start_process\""));
    assertTrue(json.contains("\"processDefinitionId\":\"process-1\""));
    assertTrue(json.contains("\"payload\":{\"key\":\"value\"}"));

   // Deserialize and verify
    LifecycleEvent deserialized = JSON.parseObject(json, LifecycleEvent.class);
    assertInstanceOf(LifecycleEvent.StartProcess.class, deserialized);
    LifecycleEvent.StartProcess typedEvent = (LifecycleEvent.StartProcess) deserialized;
    assertEquals("start_process", typedEvent.getType());
    assertEquals("process-1", typedEvent.getProcessDefinitionId());
    @SuppressWarnings("unchecked")
    Map<String, String> payload = (Map<String, String>) typedEvent.getPayload();
    assertEquals("value", payload.get("key"));
  }

  @Test
  void testStartProcessInstanceSerialization() {
   // Create event
    LifecycleEvent.StartProcessInstance event = new LifecycleEvent.StartProcessInstance();
    event.setInstanceId("instance-1");
    event.setPayload(Map.of("data", "test"));

   // Serialize
    String json = JSON.toJSONString(event);

   // Verify JSON structure
    assertTrue(json.contains("\"type\":\"start_process_instance\""));
    assertTrue(json.contains("\"instanceId\":\"instance-1\""));
    assertTrue(json.contains("\"payload\":{\"data\":\"test\"}"));

   // Deserialize and verify
    LifecycleEvent deserialized = JSON.parseObject(json, LifecycleEvent.class);
    assertInstanceOf(LifecycleEvent.StartProcessInstance.class, deserialized);
    LifecycleEvent.StartProcessInstance typedEvent =
        (LifecycleEvent.StartProcessInstance) deserialized;
    assertEquals("start_process_instance", typedEvent.getType());
    assertEquals("instance-1", typedEvent.getInstanceId());
    @SuppressWarnings("unchecked")
    Map<String, String> payload = (Map<String, String>) typedEvent.getPayload();
    assertEquals("test", payload.get("data"));
  }

  @Test
  void testContinueProcessFromTaskSerialization() {
   // Create event
    LifecycleEvent.ContinueProcessFromTask event = new LifecycleEvent.ContinueProcessFromTask();
    event.setProcessInstanceId("instance-1");
    event.setTaskId("task-1");

   // Serialize
    String json = JSON.toJSONString(event);

   // Verify JSON structure
    assertTrue(json.contains("\"type\":\"continue_process_from_task\""));
    assertTrue(json.contains("\"processInstanceId\":\"instance-1\""));
    assertTrue(json.contains("\"taskId\":\"task-1\""));

   // Deserialize and verify
    LifecycleEvent deserialized = JSON.parseObject(json, LifecycleEvent.class);
    assertInstanceOf(LifecycleEvent.ContinueProcessFromTask.class, deserialized);
    LifecycleEvent.ContinueProcessFromTask typedEvent =
        (LifecycleEvent.ContinueProcessFromTask) deserialized;
    assertEquals("continue_process_from_task", typedEvent.getType());
    assertEquals("instance-1", typedEvent.getProcessInstanceId());
    assertEquals("task-1", typedEvent.getTaskId());
  }

  @Test
  void testSignalProcessSerialization() {
   // Create event with signal
    Signal.Resume resumeSignal = Signal.Resume.with(Map.of("key", "value"));

    LifecycleEvent.SignalProcess event = new LifecycleEvent.SignalProcess();
    event.setProcessInstanceId("instance-1");
    event.setSignal(resumeSignal);

   // Serialize
    String json = JSON.toJSONString(event);

   // Verify JSON structure
    assertTrue(json.contains("\"type\":\"signal_process\""));
    assertTrue(json.contains("\"processInstanceId\":\"instance-1\""));
    assertTrue(json.contains("\"signal\":{"));
    assertTrue(json.contains("\"type\":\"resume\""));

   // Deserialize and verify
    LifecycleEvent deserialized = JSON.parseObject(json, LifecycleEvent.class);
    assertInstanceOf(LifecycleEvent.SignalProcess.class, deserialized);
    LifecycleEvent.SignalProcess typedEvent = (LifecycleEvent.SignalProcess) deserialized;
    assertEquals("signal_process", typedEvent.getType());
    assertEquals("instance-1", typedEvent.getProcessInstanceId());
    assertInstanceOf(Signal.Resume.class, typedEvent.getSignal());

    Signal.Resume deserializedSignal = (Signal.Resume) typedEvent.getSignal();
    @SuppressWarnings("unchecked")
    Map<String, String> callbackData = (Map<String, String>) deserializedSignal.getCallbackData();
    assertEquals("value", callbackData.get("key"));
  }

  @Test
  void testComplexPayloadSerialization() {
   // Test with complex nested payload
    Map<String, Object> complexPayload =
        Map.of(
            "list", List.of(1, 2, 3),
            "nested", Map.of("inner", Map.of("value", "test")));

    LifecycleEvent.StartProcess event = new LifecycleEvent.StartProcess();
    event.setProcessDefinitionId("process-1");
    event.setPayload(complexPayload);

   // Serialize and deserialize
    String json = JSON.toJSONString(event);
    LifecycleEvent deserialized = JSON.parseObject(json, LifecycleEvent.class);

    assertInstanceOf(LifecycleEvent.StartProcess.class, deserialized);
    LifecycleEvent.StartProcess typedEvent = (LifecycleEvent.StartProcess) deserialized;

    @SuppressWarnings("unchecked")
    Map<String, Object> deserializedPayload = (Map<String, Object>) typedEvent.getPayload();

    @SuppressWarnings("unchecked")
    List<Integer> list = (List<Integer>) deserializedPayload.get("list");
    assertEquals(List.of(1, 2, 3), list);

    @SuppressWarnings("unchecked")
    Map<String, Object> nested = (Map<String, Object>) deserializedPayload.get("nested");
    @SuppressWarnings("unchecked")
    Map<String, String> inner = (Map<String, String>) nested.get("inner");
    assertEquals("test", inner.get("value"));
  }

  @Test
  void testInvalidTypeSerialization() {
    String invalidJson = "{\"type\":\"invalid_type\"}";
    assertThrows(Exception.class, () -> JSON.parseObject(invalidJson, LifecycleEvent.class));
  }
}
