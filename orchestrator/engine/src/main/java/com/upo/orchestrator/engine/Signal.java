/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONType;

@JSONType(
    seeAlso = {Signal.Resume.class, Signal.Return.class, Signal.Stop.class},
    typeKey = "type",
    orders = {"type"})
public abstract sealed class Signal {

  private final String type;
  private final ProcessFlowStatus flowStatus;

  public Signal(String type, ProcessFlowStatus flowStatus) {
    this.type = type;
    this.flowStatus = flowStatus;
  }

  public String getType() {
    return type;
  }

  public ProcessFlowStatus getFlowStatus() {
    return flowStatus;
  }

  /**
   * Signal to resume process execution after a WAIT state. Used when external/async operations
   * complete and process should continue.
   */
  @JSONType(typeName = "resume")
  public static final class Resume extends Signal {
    private final Object callbackData;

    @JSONCreator(parameterNames = {"callbackData"})
    public Resume(Object callbackData) {
      super("resume", ProcessFlowStatus.CONTINUE);
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
  @JSONType(typeName = "stop")
  public static final class Stop extends Signal {
    private final Object callbackData;

    @JSONCreator(parameterNames = {"flowStatus", "callbackData"})
    public Stop(ProcessFlowStatus flowStatus, Object callbackData) {
      super("stop", flowStatus);
      if (flowStatus != ProcessFlowStatus.FAILED && flowStatus != ProcessFlowStatus.SUSPENDED) {
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
  @JSONType(typeName = "return")
  public static final class Return extends Signal {
    private final Object returnValue;

    @JSONCreator(parameterNames = {"returnValue"})
    public Return(Object returnValue) {
      super("return", ProcessFlowStatus.COMPLETED);
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
