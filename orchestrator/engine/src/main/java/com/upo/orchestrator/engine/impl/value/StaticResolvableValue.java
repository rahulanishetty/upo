/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.value;

import com.upo.orchestrator.engine.ResolvableValue;
import com.upo.orchestrator.engine.models.ProcessInstance;

/** Represents a static value that doesn't need resolution. */
public class StaticResolvableValue implements ResolvableValue {
  private final Object value;

  public StaticResolvableValue(Object value) {
    this.value = value;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public Object evaluate(ProcessInstance context) {
    return value;
  }
}
