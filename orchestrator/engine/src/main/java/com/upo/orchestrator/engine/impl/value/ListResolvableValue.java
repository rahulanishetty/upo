/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.upo.orchestrator.engine.ResolvableValue;
import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.utilities.ds.Pair;

/** Handles resolution of List structures, resolving each element independently. */
public class ListResolvableValue implements ResolvableValue {
  private final List<ResolvableValue> values;

  public ListResolvableValue(List<ResolvableValue> values) {
    this.values = values;
  }

  @Override
  public <T> T evaluate(ProcessInstance context) {
    List<Object> result = new ArrayList<>();
    for (ResolvableValue value : values) {
      Object item = value.evaluate(context);
      if (item != null) {
        result.add(item);
      }
    }
   //noinspection unchecked
    return (T) result;
  }

  @Override
  public Set<Pair<String, Variable.Type>> getVariableDependencies() {
    Set<Pair<String, Variable.Type>> result = new HashSet<>();
    for (ResolvableValue value : values) {
      result.addAll(value.getVariableDependencies());
    }
    return result;
  }
}
