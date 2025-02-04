/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

public abstract sealed class Signal {

  private final ProcessFlowStatus flowStatus;

  public Signal(ProcessFlowStatus flowStatus) {
    this.flowStatus = flowStatus;
  }

  public ProcessFlowStatus getFlowStatus() {
    return flowStatus;
  }

  /**
   * Signal to resume process execution after a WAIT state. Used when external/async operations
   * complete and process should continue.
   */
  public static final class Resume extends Signal {
    private final Object callbackData;

    public Resume(Object callbackData) {
      super(ProcessFlowStatus.CONTINUE);
      this.callbackData = callbackData;
    }

    public Object getCallbackData() {
      return callbackData;
    }

    public static Resume with(Object callbackData) {
      return new Resume(callbackData);
    }
  }

  /**
   * Signal to stop process execution either due to failure or explicit suspension. Carries reason
   * details and determines whether process failed or was suspended.
   */
  public static final class Stop extends Signal {
    private final Object callbackData;

    private Stop(ProcessFlowStatus stopStatus, Object callbackData) {
      super(stopStatus);
      if (stopStatus != ProcessFlowStatus.FAILED && stopStatus != ProcessFlowStatus.SUSPENDED) {
        throw new IllegalArgumentException("Stop signal must use FAILED or SUSPENDED status");
      }
      this.callbackData = callbackData;
    }

    public Object getCallbackData() {
      return callbackData;
    }

    public static Stop failed(Object callbackData) {
      return new Stop(ProcessFlowStatus.FAILED, callbackData);
    }

    public static Stop suspended() {
      return new Stop(ProcessFlowStatus.SUSPENDED, null);
    }
  }

  /**
   * Signal carrying a return value from process execution. Used for explicit process termination
   * with a value.
   */
  public static final class Return extends Signal {
    private final Object returnValue;

    public Return(Object returnValue) {
      super(ProcessFlowStatus.COMPLETED);
      this.returnValue = returnValue;
    }

    public Object getReturnValue() {
      return returnValue;
    }

    public static Return with(Object value) {
      return new Return(value);
    }
  }
}
