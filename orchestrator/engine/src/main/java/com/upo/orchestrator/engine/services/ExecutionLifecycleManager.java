/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.services;

import com.upo.orchestrator.engine.ProcessFlowStatus;
import com.upo.orchestrator.engine.TaskRuntime;
import com.upo.orchestrator.engine.models.ProcessInstance;

public interface ExecutionLifecycleManager {

  void signalProcess(
      ProcessInstance processInstance,
      ProcessInstance targetInstance,
      ProcessFlowStatus flowStatus,
      Object payload);

  void resumeProcessFromTask(ProcessInstance processInstance, TaskRuntime taskRuntime);

  void cleanup(ProcessInstance processInstance);
}
