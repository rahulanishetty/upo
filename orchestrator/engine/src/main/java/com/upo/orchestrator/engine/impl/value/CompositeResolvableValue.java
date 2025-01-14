/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.value;

import java.util.List;

import com.upo.orchestrator.engine.ResolvableValue;
import com.upo.orchestrator.engine.models.ProcessInstance;

/**
 * Combines multiple ResolvableValue implementations into a single value. Used for strings that
 * contain multiple variable references or expressions.
 */
public class CompositeResolvableValue implements ResolvableValue {
  private final List<ResolvableValue> parts;

  public CompositeResolvableValue(List<ResolvableValue> parts) {
    this.parts = parts;
  }

  @Override
  public Object evaluate(ProcessInstance context) {
    StringBuilder result = new StringBuilder();

    for (ResolvableValue part : parts) {
      Object evaluated = part.evaluate(context);
      result.append(evaluated != null ? evaluated.toString() : "");
    }

    return result.toString();
  }
}
