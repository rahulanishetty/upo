/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.rt;

import static com.upo.orchestrator.engine.impl.callbacks.StartSubProcessProcessInstanceCallbackBuilder.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.upo.orchestrator.engine.ProcessRuntime;
import com.upo.orchestrator.engine.TaskResult;
import com.upo.orchestrator.engine.impl.ProcessExecutorImpl;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.ExecutionLifecycleManager;
import com.upo.orchestrator.engine.services.ProcessInstanceStore;
import com.upo.orchestrator.engine.services.ProcessManager;
import com.upo.utilities.ds.CollectionUtils;

public class StartSubProcessRuntime extends AbstractTaskOrchestrationRuntime {

  public StartSubProcessRuntime(ProcessRuntime parent, String taskId) {
    super(parent, taskId);
  }

  @Override
  protected TaskResult doExecute(ProcessInstance processInstance) {
    Map<String, Object> processed = inputs.evaluate(processInstance);
    boolean async = CollectionUtils.getBooleanValue(processed, "async", false);
    if (async) {
      return startProcessAsync(processInstance, processed);
    }
    return startProcess(processInstance, processed);
  }

  private TaskResult.Wait startProcess(
      ProcessInstance processInstance, Map<String, Object> processed) {
    String processId = CollectionUtils.getStringValue(processed, "processId");
    Object payload = CollectionUtils.getValue(processed, "payload");

    ProcessInstance childInstance = createAndSaveChildProcessInstance(processInstance, processId);
    Map<String, Object> callbackData = createCallbackData(childInstance, payload);
    return new TaskResult.Wait(
        Collections.singletonList(toInputVariable(processed)), TYPE, callbackData);
  }

  private static Map<String, Object> createCallbackData(
      ProcessInstance childInstance, Object payload) {
    Map<String, Object> callbackData = new HashMap<>();
    callbackData.put(INSTANCE_ID, childInstance.getId());
    callbackData.put(PAYLOAD, payload);
    return callbackData;
  }

  private ProcessInstance createAndSaveChildProcessInstance(
      ProcessInstance processInstance, String processId) {
    ProcessManager processManager = getService(processInstance, ProcessManager.class);
    ProcessRuntime processRuntime = processManager.getOrCreateRuntime(processId);
    ProcessInstance childInstance =
        ProcessExecutorImpl.createChildInstance(processInstance, processRuntime.getDetails());
    ProcessInstanceStore processInstanceStore =
        getService(processInstance, ProcessInstanceStore.class);
    if (!processInstanceStore.save(childInstance)) {
      throw new IllegalStateException("failed to save child process instance!");
    }
    return childInstance;
  }

  private TaskResult.Continue startProcessAsync(
      ProcessInstance processInstance, Map<String, Object> processed) {
    ExecutionLifecycleManager lifecycleManager =
        getService(processInstance, ExecutionLifecycleManager.class);
    String processId = CollectionUtils.getStringValue(processed, "processId");
    Object payload = CollectionUtils.getValue(processed, "payload");
    lifecycleManager.startProcess(processId, payload);
    return TaskResult.Continue.with(Collections.singletonList(toInputVariable(processed)));
  }
}
