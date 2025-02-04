/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.rt;

import java.util.Collections;

import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.models.ProcessInstance;

public class StopProcessRuntime extends AbstractTaskOrchestrationRuntime {

  public StopProcessRuntime(ProcessRuntime parent, String taskId) {
    super(parent, taskId);
    super.setOutgoingTransitions((_, _, _) -> Collections.emptyList());
  }

  @Override
  public void setOutgoingTransitions(TransitionResolver outgoingTransitions) {
   // no-op
  }

  @Override
  protected TaskResult doExecute(ProcessInstance processInstance) {
    if (inputs != null) {
      Object returnValue = inputs.evaluate(processInstance);
      return TaskResult.ReturnResult.with(
          returnValue, Collections.singletonList(toOutput(returnValue)));
    }
    return TaskResult.Continue.with(Collections.emptyList());
  }
}
