/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.value;

import java.util.LinkedHashMap;
import java.util.Map;

import com.upo.orchestrator.engine.ResolvableValue;
import com.upo.orchestrator.engine.models.ProcessInstance;

/** Handles resolution of Map structures, resolving each value independently. */
public class MapResolvableValue implements ResolvableValue {
  private final Map<String, ResolvableValue> valueMap;

  public MapResolvableValue(Map<String, ResolvableValue> valueMap) {
    this.valueMap = valueMap;
  }

  @Override
  public Object evaluate(ProcessInstance context) {
    Map<String, Object> result = new LinkedHashMap<>();
    for (Map.Entry<String, ResolvableValue> entry : valueMap.entrySet()) {
      Object value = entry.getValue().evaluate(context);
      if (value != null) {
        result.put(entry.getKey(), value);
      }
    }
    return result;
  }
}
