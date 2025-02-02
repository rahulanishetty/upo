/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.value;

import java.util.*;

import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.impl.CompositeVariableView;
import com.upo.orchestrator.engine.impl.ImmutableVariableContainer;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.utilities.ds.CollectionUtils;
import com.upo.utilities.ds.Pair;

/**
 * Resolver that handles array transformations in process definitions. This resolver enables
 * transforming each element of a source array into a new structure using a template pattern.
 *
 * <p>The transformation is defined using a JSON structure with two main components:
 *
 * <ul>
 *   <li>source: A reference to the source array to transform
 *   <li>item: A template defining how to transform each element
 * </ul>
 *
 * Example JSON definition:
 *
 * <pre>
 * {
 *   "__@type": "arrayTransform",
 *   "source": "{{ task.input.users }}",
 *   "item": {
 *     "id": "{{ item.userId }}",
 *     "name": "{{ item.fullName }}"
 *   }
 * }
 * </pre>
 *
 * In the template, each source array element is accessible via the 'item' variable. The
 * transformation supports all standard variable resolution patterns including direct references and
 * expressions.
 *
 * @see ResolvableValue
 * @see InputValueResolver
 */
public class ArrayTransformResolver implements InputValueResolver {

  private final InputValueResolver resolver;

  public ArrayTransformResolver(InputValueResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public ResolvableValue resolve(Object inputObj) {
   //noinspection unchecked
    Map<String, Object> input = (Map<String, Object>) inputObj;
    String source = CollectionUtils.getStringValue(input, "source");
    ResolvableValue sourceValue;
    if (source != null) {
      sourceValue = resolver.resolve(source);
    } else {
      sourceValue = new StaticResolvableValue(Collections.singletonList(null));
    }
    Object item = CollectionUtils.getValue(input, "item");
    if (item == null) {
      return null;
    }
    return new ArrayTransformValue(sourceValue, resolver.resolve(item));
  }

  /**
   * Implements the array transformation logic, transforming source array elements according to a
   * template. For each element in the source array, creates a new variable scope making the element
   * available as 'item', then evaluates the template in this scope.
   */
  @SuppressWarnings("ClassCanBeRecord")
  private static class ArrayTransformValue implements ResolvableValue {

    private final ResolvableValue source;
    private final ResolvableValue item;

    public ArrayTransformValue(ResolvableValue source, ResolvableValue item) {
      this.source = source;
      this.item = item;
    }

    @Override
    public <T> T evaluate(ProcessInstance context) {
      Object evaluate = source.evaluate(context);
      if (evaluate == null) {
        return null;
      }
      if (!(evaluate instanceof List<?> list)) {
        return null;
      }
      List<Object> result = new ArrayList<>();
      VariableContainer original = context.getVariableContainer();
      for (Object item : list) {
        try {
          CompositeVariableView view = createVariableViewWithItem(item, original);
          context.setVariableContainer(view);
          Object resolvedItem = this.item.evaluate(context);
          if (resolvedItem != null) {
            result.add(resolvedItem);
          }
        } finally {
          context.setVariableContainer(original);
        }
      }
     //noinspection unchecked
      return (T) result;
    }

    @Override
    public Set<Pair<String, Variable.Type>> getVariableDependencies() {
      Set<Pair<String, Variable.Type>> result = new HashSet<>(source.getVariableDependencies());
      for (Pair<String, Variable.Type> itemDependency : item.getVariableDependencies()) {
        if (!Objects.equals("item", itemDependency.getFirstElement())) {
          result.add(itemDependency);
        }
      }
      return result;
    }

    private CompositeVariableView createVariableViewWithItem(
        Object item, VariableContainer original) {
      CompositeVariableView view = new CompositeVariableView();
      VariableContainer itemContainer = new ImmutableVariableContainer();
      itemContainer.restoreVariable("item", null, item);
      view.addVariables(itemContainer);
      view.addVariables(original);
      return view;
    }
  }
}
