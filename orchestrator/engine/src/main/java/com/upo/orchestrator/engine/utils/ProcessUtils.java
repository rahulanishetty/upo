/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.upo.orchestrator.engine.TaskResult;
import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.models.ProcessVariable;

public class ProcessUtils {

  public static TaskResult toTaskResult(
      TaskResult.Status status, String taskId, Map<String, Object> output) {
    List<Variable> variables = null;
    if (output != null) {
      ProcessVariable variable = new ProcessVariable();
      variable.setTaskId(taskId);
      variable.setType(Variable.Type.OUTPUT);
      variable.setPayload(output);
      variables = Collections.singletonList(variable);
    }
    return switch (status) {
      case WAIT -> new TaskResult.Wait(variables);
      case CONTINUE -> TaskResult.Continue.with(variables);
      case ERROR -> TaskResult.Error.with(variables);
    };
  }

  public static boolean isRootInstance(ProcessInstance processInstance) {
    return processInstance.getRootId() == null;
  }
}
