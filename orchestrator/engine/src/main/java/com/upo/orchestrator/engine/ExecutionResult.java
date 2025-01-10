/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.upo.orchestrator.engine.models.CompletionSignal;

/**
 * Represents the result of a task execution step. Supports multi-phase execution patterns like
 * two-phase commit and post-persistence callbacks.
 */
public class ExecutionResult {

  public static final ExecutionResult CONTINUE = new ExecutionResult(Status.CONTINUE);

  /** Status of task execution. */
  public enum Status {
    /**
     * Task has completed and process should continue with normal execution flow. Next task in the
     * process should be executed immediately.
     */
    CONTINUE,
    /** Wait for signal */
    WAIT,

    /**
     * Task has reached terminal state (success or failure). Process execution should end with the
     * outcome specified in completion signal.
     */
    TERMINAL;
  }

  private Status status;

  /**
   * Completion details when task reaches TERMINAL status. Contains information about: -
   * Success/failure outcome - Error details if failed - Completion metadata - Terminal state reason
   *
   * <p>Only relevant when status is TERMINAL.
   */
  private CompletionSignal completionSignal;

  /**
   * Serializable command to be executed after state persistence. This allows defining
   * post-persistence actions without coupling to specific implementation.
   */
  private Map<String, Object> afterSaveCommand;

  /**
   * Variables produced during this execution step. Captures: - Task inputs after variable
   * resolution - Task outputs after execution - Error information if task failed - State changes
   * during execution
   */
  private Collection<Variable> variables;

  public ExecutionResult() {}

  public ExecutionResult(Status status) {
    this.status = status;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public CompletionSignal getCompletionSignal() {
    return completionSignal;
  }

  public void setCompletionSignal(CompletionSignal completionSignal) {
    this.completionSignal = completionSignal;
  }

  public Map<String, Object> getAfterSaveCommand() {
    return afterSaveCommand;
  }

  public void setAfterSaveCommand(Map<String, Object> afterSaveCommand) {
    this.afterSaveCommand = afterSaveCommand;
  }

  public Collection<Variable> getVariables() {
    return variables;
  }

  public void setVariables(Collection<Variable> variables) {
    this.variables = variables;
  }

  /** Convenience method to add a single variable */
  public void addVariable(Variable variable) {
    if (this.variables == null) {
      this.variables = new ArrayList<>();
    }
    this.variables.add(variable);
  }
}
