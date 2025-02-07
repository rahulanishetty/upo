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

/** Represents the final outcome of a process execution. */
@JSONType(
    typeKey = "type",
    seeAlso = {ProcessOutcome.Success.class, ProcessOutcome.Failure.class},
    orders = {"type"})
public interface ProcessOutcome {

  String getType();

  @SuppressWarnings("ClassCanBeRecord")
  @JSONType(typeName = "SUCCESS")
  class Success implements ProcessOutcome {

    private final Object result;

    @JSONCreator(parameterNames = {"result"})
    public Success(Object result) {
      this.result = result;
    }

    public Object getResult() {
      return result;
    }

    @Override
    public String getType() {
      return "SUCCESS";
    }
  }

  @SuppressWarnings("ClassCanBeRecord")
  @JSONType(typeName = "FAILURE")
  class Failure implements ProcessOutcome {

    private final Object failure;

    @JSONCreator(parameterNames = {"failure"})
    public Failure(Object failure) {
      this.failure = failure;
    }

    public Object getFailure() {
      return failure;
    }

    @Override
    public String getType() {
      return "FAILURE";
    }
  }
}
