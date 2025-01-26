/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import java.util.List;

public class ProcessFlowResult {
  private final TaskResult taskResult;// Raw task execution result
  private final ProcessFlowStatus flowStatus;// Process flow status
  private final List<Transition> nextTransitions;

  public ProcessFlowResult(
      TaskResult taskResult, ProcessFlowStatus flowStatus, List<Transition> nextTransitions) {
    this.taskResult = taskResult;
    this.flowStatus = flowStatus;
    this.nextTransitions = nextTransitions;
  }

  public TaskResult getTaskResult() {
    return taskResult;
  }

  public ProcessFlowStatus getFlowStatus() {
    return flowStatus;
  }

  public List<Transition> getNextTransitions() {
    return nextTransitions;
  }
}
