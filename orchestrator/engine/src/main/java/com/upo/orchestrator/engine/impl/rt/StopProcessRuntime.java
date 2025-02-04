/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.rt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    List<Variable> variables = new ArrayList<>();
    Object returnValue = null;
    if (inputs != null) {
      returnValue = inputs.evaluate(processInstance);
      variables.add(toOutput(returnValue));
    }
    return TaskResult.ReturnResult.with(returnValue, variables);
  }
}
