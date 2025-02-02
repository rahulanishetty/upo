/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.value;

import java.util.Collections;
import java.util.Set;

import com.upo.orchestrator.engine.ResolvableValue;
import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.utilities.ds.Pair;
import com.upo.utilities.json.path.JsonPath;

/** ResolvableValue implementation for variable references. */
public class VariableResolvableValue implements ResolvableValue {
  private final JsonPath variablePath;

  public VariableResolvableValue(String variablePath) {
    variablePath = variablePath.trim();
    if (variablePath.isEmpty()) {
      throw new IllegalArgumentException("variable cannot be blank");
    }
    this.variablePath = JsonPath.create(variablePath);
  }

  @Override
  public <T> T evaluate(ProcessInstance context) {
   //noinspection unchecked
    return (T) context.getVariableContainer().readVariable(variablePath);
  }

  @Override
  public Set<Pair<String, Variable.Type>> getVariableDependencies() {
    Pair<String, Variable.Type> pair = fromJsonPath(variablePath);
    return Collections.singleton(pair);
  }

  public static Pair<String, Variable.Type> fromJsonPath(JsonPath jsonPath) {
    String taskId = jsonPath.getToken(0);
    Variable.Type type = Variable.Type.fromKey(jsonPath.getToken(1));
    return Pair.of(taskId, type);
  }
}
