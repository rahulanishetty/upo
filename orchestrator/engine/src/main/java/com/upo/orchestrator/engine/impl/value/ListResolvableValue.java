/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.value;

import java.util.ArrayList;
import java.util.List;

import com.upo.orchestrator.engine.ResolvableValue;
import com.upo.orchestrator.engine.models.ProcessInstance;

/** Handles resolution of List structures, resolving each element independently. */
public class ListResolvableValue implements ResolvableValue {
  private final List<ResolvableValue> values;

  public ListResolvableValue(List<ResolvableValue> values) {
    this.values = values;
  }

  @Override
  public Object evaluate(ProcessInstance context) {
    List<Object> result = new ArrayList<>();
    for (ResolvableValue value : values) {
      Object item = value.evaluate(context);
      if (item != null) {
        result.add(item);
      }
    }
    return result;
  }
}
