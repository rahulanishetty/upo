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

/**
 * An optimized implementation of ResolvableValue for Map structures that separates static and
 * dynamic values while preserving the original map's key ordering.
 *
 * <p>This implementation: 1. Maintains static values that don't need resolution 2. Separately
 * tracks dynamic values that require resolution 3. Preserves original map key ordering through
 * LinkedHashMap 4. Optimizes evaluation by only resolving dynamic values
 *
 * <p>Key behaviors: - Original map ordering is preserved through static map structure - Dynamic
 * values are resolved only when evaluate() is called - Null values in static map indicate presence
 * of dynamic value
 */
public class OptimizedMapResolvableValue implements ResolvableValue {
  private final Map<String, Object> staticValues;
  private final Map<String, ResolvableValue> dynamicValues;

  public OptimizedMapResolvableValue(
      Map<String, Object> staticValues, Map<String, ResolvableValue> dynamicValues) {
    this.staticValues = staticValues;
    this.dynamicValues = dynamicValues;
  }

  @Override
  public Object evaluate(ProcessInstance context) {
   // Copy static map - this preserves order
    Map<String, Object> result = new LinkedHashMap<>(staticValues);

   // Resolve and overwrite dynamic values
    for (Map.Entry<String, ResolvableValue> entry : dynamicValues.entrySet()) {
      Object evaluate = entry.getValue().evaluate(context);
      if (evaluate == null) {
        result.remove(entry.getKey());
      } else {
        result.put(entry.getKey(), evaluate);
      }
    }
    return result;
  }
}
