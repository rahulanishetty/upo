/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import java.util.*;

/**
 * Represents the result of a task execution step. Supports multi-phase execution patterns like
 * two-phase commit and post-persistence callbacks.
 */
public abstract sealed class TaskResult {

  /** Status of task execution. */
  public enum Status {
    /**
     * Task has completed and process should continue with normal execution flow. Next task in the
     * process should be executed immediately.
     */
    CONTINUE,
    /** Wait for signal */
    WAIT,
    /** Task execution failed with error. */
    FAIL;
  }

  private Status status;

  /**
   * Variables produced during this execution step. Captures: - Task inputs after variable
   * resolution - Task outputs after execution - Error information if task failed - State changes
   * during execution
   */
  private Collection<Variable> variables;

  public TaskResult() {}

  public TaskResult(Status status) {
    this.status = status;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
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

  public static sealed class Continue extends TaskResult {
    private Continue(Collection<Variable> variables) {
      super(Status.CONTINUE);
      setVariables(variables);
    }

    public static Continue with(Collection<Variable> variables) {
      return new Continue(variables);
    }
  }

  public static final class ContinueWithTransitions extends Continue {

    private final List<Transition> transitions;

    private ContinueWithTransitions(Collection<Variable> variables, List<Transition> transitions) {
      super(variables);
      this.transitions = transitions;
    }

    public List<Transition> getTransitions() {
      return transitions;
    }

    public static ContinueWithTransitions with(
        Collection<Variable> variables, List<Transition> transitions) {
      return new ContinueWithTransitions(variables, transitions);
    }
  }

  public static final class Fail extends TaskResult {
    private Fail(Collection<Variable> variables) {
      super(Status.FAIL);
      setVariables(variables);
    }

    public static Fail with(Collection<Variable> variables) {
      return new Fail(variables);
    }
  }

  /**
   * Represents a task result that includes a return value, signaling process termination. This
   * result type is used by special tasks that can terminate process execution with a value.
   * Although it uses Status.CONTINUE, the presence of a return value indicates early termination.
   *
   * <p>The task result carries: - A return value of any type that will be propagated to the process
   * caller - Variables produced during task execution
   */
  public static final class ReturnResult extends Continue {
    private final Object returnValue;

    public ReturnResult(Object value, Collection<Variable> variables) {
      super(variables);
      this.returnValue = value;
    }

    public Object getReturnValue() {
      return returnValue;
    }

    public static ReturnResult with(Object value, Collection<Variable> variables) {
      return new ReturnResult(value, variables);
    }
  }

  /**
   * Task result indicating process should enter WAIT state and execute post-commit operations. Used
   * in distributed scenarios where operations must be performed only after successful state
   * persistence.
   *
   * <p>Example usage: - Spawning child processes - Sending messages to external systems -
   * Initiating remote task execution
   */
  public static final class Wait extends TaskResult {
    /**
     * Identifier for the ProcessCallback implementation. Must be registered with
     * ProcessCallbackFactory.
     */
    private String callbackType;

    /** Operation parameters for callback execution. */
    private Map<String, Object> callbackData;

    public Wait() {
      super(Status.WAIT);
    }

    public Wait(String callbackType, Map<String, Object> callbackData) {
      super(Status.WAIT);
      this.callbackType = callbackType;
      this.callbackData = callbackData;
    }

    public Wait(
        Collection<Variable> variables, String callbackType, Map<String, Object> callbackData) {
      this(callbackType, callbackData);
      setVariables(variables);
    }

    public Wait(Collection<Variable> variables) {
      this();
      setVariables(variables);
    }

    public String getCallbackType() {
      return callbackType;
    }

    public void setCallbackType(String callbackType) {
      this.callbackType = callbackType;
    }

    public Map<String, Object> getCallbackData() {
      return callbackData;
    }

    public void setCallbackData(Map<String, Object> callbackData) {
      this.callbackData = callbackData;
    }
  }
}
