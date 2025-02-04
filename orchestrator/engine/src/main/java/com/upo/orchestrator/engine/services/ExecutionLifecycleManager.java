/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.services;

import com.upo.orchestrator.engine.ProcessOutcome;
import com.upo.orchestrator.engine.Signal;
import com.upo.orchestrator.engine.models.ProcessInstance;

public interface ExecutionLifecycleManager {

  void startProcess(String processDefinitionId, Object payload);

  void startInstanceWithPayload(String instanceId, Object payload);

  void signalProcess(
      ProcessInstance processInstance, ProcessInstance targetInstance, Signal signal);

  void signalProcess(ProcessInstance processInstance, String targetInstanceId, Signal signal);

  void continueProcessFromTask(String processInstanceId, String taskId);

  void cleanup(ProcessInstance processInstance);

  void notifyCompletion(ProcessInstance processInstance, ProcessOutcome processOutcome);
}
