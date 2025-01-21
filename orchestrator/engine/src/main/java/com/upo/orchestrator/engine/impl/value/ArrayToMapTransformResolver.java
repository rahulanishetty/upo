/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.value;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.upo.orchestrator.engine.ImmutableVariableContainer;
import com.upo.orchestrator.engine.InputValueResolver;
import com.upo.orchestrator.engine.ResolvableValue;
import com.upo.orchestrator.engine.VariableContainer;
import com.upo.orchestrator.engine.impl.CompositeVariableView;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.utilities.ds.CollectionUtils;

public class ArrayToMapTransformResolver implements InputValueResolver {

  private final InputValueResolver resolver;

  public ArrayToMapTransformResolver(InputValueResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public ResolvableValue resolve(Object inputObj) {
   //noinspection unchecked
    Map<String, Object> input = (Map<String, Object>) inputObj;
    String source = CollectionUtils.getStringValue(input, "source");
    Object key = input.get("key");
    Object value = input.get("value");

    return new ArrayToMapTransformValue(
        resolver.resolve(source), resolver.resolve(key), resolver.resolve(value));
  }

  /**
   * ResolvableValue implementation that performs array to map transformation. For each element in
   * the source array, extracts a key and value according to the specified expressions and builds a
   * map from these pairs.
   */
  @SuppressWarnings("ClassCanBeRecord")
  private static class ArrayToMapTransformValue implements ResolvableValue {

    private final ResolvableValue source;
    private final ResolvableValue keyExpr;
    private final ResolvableValue valueExpr;

    public ArrayToMapTransformValue(
        ResolvableValue source, ResolvableValue keyExpr, ResolvableValue valueExpr) {
      this.source = source;
      this.keyExpr = keyExpr;
      this.valueExpr = valueExpr;
    }

    @Override
    public Object evaluate(ProcessInstance context) {
      Object sourceValue = source.evaluate(context);
      if (sourceValue == null) {
        return null;
      }
      if (!(sourceValue instanceof List<?> list)) {
        return null;
      }

      Map<Object, Object> result = new LinkedHashMap<>();
      VariableContainer original = context.getVariableContainer();

      for (Object item : list) {
        try {
         // Create new scope with current item
          CompositeVariableView view = createVariableViewWithItem(item, original);
          context.setVariableContainer(view);

         // Extract key and value using expressions
          Object key = keyExpr.evaluate(context);
          if (key != null) {// Skip entries with null keys
            Object value = valueExpr.evaluate(context);
            if (value != null) {
              result.put(key, value);
            }
          }
        } finally {
         // Restore original scope
          context.setVariableContainer(original);
        }
      }
      return result;
    }

    private CompositeVariableView createVariableViewWithItem(
        Object item, VariableContainer original) {
      CompositeVariableView view = new CompositeVariableView();
      VariableContainer variable = new ImmutableVariableContainer();
      variable.restoreVariable("item", null, item);
      view.addVariables(variable);
      view.addVariables(original);
      return view;
    }
  }
}
