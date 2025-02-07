/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import com.alibaba.fastjson2.annotation.JSONType;
import com.upo.orchestrator.engine.impl.PromiseCompletingOutcomeSink;
import com.upo.orchestrator.engine.impl.RedisBackedOutcomeSink;
import com.upo.orchestrator.engine.models.ProcessInstance;

/** Base interface for process outcome sinks with serialization awareness. */
@JSONType(
    seeAlso = {RedisBackedOutcomeSink.class, PromiseCompletingOutcomeSink.class},
    typeKey = "type",
    orders = {"type"})
public abstract class ProcessOutcomeSink {

  private final String type;

  public ProcessOutcomeSink(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public abstract void onOutcome(ProcessInstance processInstance, ProcessOutcome outcome);

  /** Indicates if this sink can be serialized for distributed execution. */
  public abstract boolean isSerializable();
}
