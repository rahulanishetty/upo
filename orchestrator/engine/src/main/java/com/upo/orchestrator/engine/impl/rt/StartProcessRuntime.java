/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.rt;

import java.util.Collections;

import com.upo.orchestrator.engine.ProcessRuntime;
import com.upo.orchestrator.engine.TaskResult;
import com.upo.orchestrator.engine.models.ProcessInstance;

public class StartProcessRuntime extends AbstractTaskOrchestrationRuntime {

  public StartProcessRuntime(ProcessRuntime parent, String taskId) {
    super(parent, taskId);
  }

  @Override
  protected TaskResult doExecute(ProcessInstance processInstance) {
    return TaskResult.Continue.with(
        Collections.singletonList(toOutput(processInstance.getInput())));
  }
}
