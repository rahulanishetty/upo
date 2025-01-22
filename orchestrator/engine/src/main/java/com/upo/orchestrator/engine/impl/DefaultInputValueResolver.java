/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.*;

import com.upo.orchestrator.engine.InputValueResolver;
import com.upo.orchestrator.engine.ResolvableValue;
import com.upo.orchestrator.engine.impl.value.*;

import jakarta.inject.Singleton;

@Singleton
public class DefaultInputValueResolver implements InputValueResolver {

  public static final String TYPE_FIELD = "__@type";

  private final Map<String, InputValueResolver> customResolvers;

  public DefaultInputValueResolver() {
    this.customResolvers =
        Map.of(
            "arrayTransformer", new ArrayTransformResolver(this),
            "arrayToMapTransformer", new ArrayToMapTransformResolver(this));
  }

  @Override
  public ResolvableValue resolve(Object input) {
    if (input == null) {
      return null;
    }
    return resolveValue(input);
  }

  /**
   * Resolves different types of values into appropriate ResolvableValue implementations.
   *
   * @param value The input value to resolve
   * @return ResolvableValue implementation for the input
   */
  private ResolvableValue resolveValue(Object value) {
    return switch (value) {
      case null -> null;
      case Map<?, ?> map -> resolveMap(map);
      case List<?> list -> resolveList(list);
      case String s -> resolveString(s);
      default -> new StaticResolvableValue(value);
    };
  }

  /** Resolves a Map into a MapResolvableValue, processing all nested values. */
  private ResolvableValue resolveMap(Map<?, ?> map) {
    if (map.isEmpty()) {
      return new StaticResolvableValue(Collections.emptyMap());
    }
    Object type = map.get(TYPE_FIELD);
    if (type instanceof String typeStr) {
      InputValueResolver inputValueResolver = customResolvers.get(typeStr);
      if (inputValueResolver != null) {
        return inputValueResolver.resolve(map);
      }
    }
    Map<String, Object> staticValues = new LinkedHashMap<>(map.size());
    Map<String, ResolvableValue> dynamicValues = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      String key = entry.getKey().toString();
      ResolvableValue resolvedValue = resolveValue(entry.getValue());
      if (resolvedValue == null) {
        staticValues.put(key, null);
      } else if (resolvedValue instanceof StaticResolvableValue staticValue) {
        staticValues.put(key, staticValue.getValue());
      } else {
        staticValues.put(key, null);// Put null in static map to maintain order
        dynamicValues.put(key, resolvedValue);
      }
    }
    if (dynamicValues.isEmpty()) {
      return new StaticResolvableValue(staticValues);
    }
    return new OptimizedMapResolvableValue(staticValues, dynamicValues);
  }

  /** Resolves a List into a ListResolvableValue, processing all elements. */
  private ResolvableValue resolveList(List<?> list) {
    if (list.isEmpty()) {
      return new StaticResolvableValue(Collections.emptyList());
    }
    List<ResolvableValue> resolvedList = new ArrayList<>();
    boolean hasAnyNonStatic = false;
    for (Object item : list) {
      ResolvableValue resolvableValue = resolveValue(item);
      if (resolvableValue != null) {
        hasAnyNonStatic |= !(resolvableValue instanceof StaticResolvableValue);
        resolvedList.add(resolvableValue);
      }
    }
    if (!hasAnyNonStatic) {
      return new StaticResolvableValue(list);
    }
    return new ListResolvableValue(resolvedList);
  }

  /**
   * Resolves a String value, handling variable references and expressions. Returns appropriate
   * ResolvableValue implementation based on content.
   */
  private ResolvableValue resolveString(String value) {
   // Parse the string and create a composite value if needed
    List<ResolvableValue> parts = ValueParser.parse(value);
    if (parts.size() == 1) {
      return parts.getFirst();
    }
    return new CompositeResolvableValue(parts);
  }
}
