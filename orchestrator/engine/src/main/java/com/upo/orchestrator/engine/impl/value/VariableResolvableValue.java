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
import com.upo.utilities.json.path.JsonPath;

/** ResolvableValue implementation for variable references. */
public class VariableResolvableValue implements ResolvableValue {
  private final JsonPath variablePath;

  public VariableResolvableValue(String variablePath) {
    this.variablePath = JsonPath.create(variablePath.trim());
  }

  @Override
  public Object evaluate(ProcessInstance context) {
    return context.getVariableContainer().readVariable(variablePath);
  }
}
