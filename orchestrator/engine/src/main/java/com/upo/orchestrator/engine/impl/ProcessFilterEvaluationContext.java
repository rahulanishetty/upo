/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import com.upo.orchestrator.engine.ResolvableValue;
import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.InputValueResolver;
import com.upo.utilities.ds.Pair;
import com.upo.utilities.filter.impl.ComparableValue;
import com.upo.utilities.filter.impl.Field;
import com.upo.utilities.filter.impl.FilterContext;
import com.upo.utilities.filter.impl.RecordAware;

/**
 * Implements FilterContext for ProcessInstance, providing field resolution and value comparison
 * capabilities within a process execution context. This class handles variable resolution,
 * dependency tracking, and value conversion for process filter evaluation.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li>Resolves field references to evaluable fields
 *   <li>Tracks variable dependencies during resolution
 *   <li>Converts values to comparable formats
 *   <li>Handles collection and scalar value evaluation
 * </ul>
 */
public class ProcessFilterEvaluationContext implements FilterContext<ProcessInstance> {

  private final Set<Pair<String, Variable.Type>> dependencies;
  private final InputValueResolver inputValueResolver;

  /**
   * Creates a new ProcessFilterEvaluationContext with the specified value resolver.
   *
   * @param inputValueResolver resolver for converting input values to ResolvableValue instances
   */
  public ProcessFilterEvaluationContext(InputValueResolver inputValueResolver) {
    this.dependencies = new HashSet<>();
    this.inputValueResolver = inputValueResolver;
  }

  /**
   * Resolves a field identifier into a Field instance that can evaluate against ProcessInstance.
   * Tracks any variable dependencies encountered during resolution.
   *
   * @param field the field identifier to resolve
   * @return a Field instance that can evaluate the resolved field
   */
  @Override
  public Field<ProcessInstance> resolveField(String field) {
    ResolvableValue fieldValue = resolveAndTrackDependencies(field);
    return new Field<>() {
      @Override
      public ComparableValue<?> toComparable(Object value) {
        ResolvableValue resolvableValue = resolveAndTrackDependencies(value);
        if (resolvableValue == null) {
          return null;
        }
        return new RecordAwareComparable(resolvableValue);
      }

      @Override
      public Collection<ComparableValue<?>> resolveValues(ProcessInstance processInstance) {
        if (fieldValue == null) {
          return Collections.emptyList();
        }
        Object value = fieldValue.evaluate(processInstance);
        Collection<?> collection;
        if (value instanceof Collection<?>) {
          collection = (Collection<?>) value;
        } else if (value.getClass().isArray()) {
          collection = Arrays.asList((Object[]) value);
        } else {
          collection = Collections.singletonList(value);
        }
        return collection.stream()
            .map(ProcessFilterEvaluationContext::_toComparable)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
      }
    };
  }

  public Set<Pair<String, Variable.Type>> getVariableDependencies() {
    return Collections.unmodifiableSet(dependencies);
  }

  /**
   * Converts an input value into a ResolvableValue while tracking variable dependencies. This
   * method performs two operations: 1. Resolves the input into a ResolvableValue using the
   * inputValueResolver 2. Tracks any variable dependencies discovered during resolution
   *
   * @param input The input value to be resolved. Can be a literal value, variable reference, or
   *     expression that needs resolution
   * @return A ResolvableValue that can be evaluated at runtime, or null if resolution fails or
   *     input is null
   * @see ResolvableValue
   */
  private ResolvableValue resolveAndTrackDependencies(Object input) {
    ResolvableValue resolvableValue = inputValueResolver.resolve(input);
    if (resolvableValue == null) {
      return null;
    }
    dependencies.addAll(resolvableValue.getVariableDependencies());
    return resolvableValue;
  }

  private static ComparableValue<?> _toComparable(Object obj) {
    return switch (obj) {
      case null -> null;
      case Boolean b -> ComparableValue.fromBoolean(b);
      case String str -> ComparableValue.fromString(str);
      case Byte b -> ComparableValue.fromByte(b);
      case Short s -> ComparableValue.fromShort(s);
      case Integer i -> ComparableValue.fromInteger(i);
      case Long l -> ComparableValue.fromLong(l);
      case BigInteger bI -> ComparableValue.fromBigInteger(bI);
      case Float f -> ComparableValue.fromFloat(f);
      case Double d -> ComparableValue.fromDouble(d);
      case BigDecimal bD -> ComparableValue.fromBigDecimal(bD);
      default -> throw new IllegalArgumentException("Unsupported data type: " + obj.getClass());
    };
  }

  @SuppressWarnings("ClassCanBeRecord")
  private static class RecordAwareComparable
      implements RecordAware<ProcessInstance>, ComparableValue<Boolean> {

    private final ResolvableValue resolvableValue;

    public RecordAwareComparable(ResolvableValue resolvableValue) {
      this.resolvableValue = resolvableValue;
    }

    @Override
    public <Value extends Comparable<Value>> ComparableValue<Value> resolve(
        ProcessInstance processInstance) {
     //noinspection unchecked
      return (ComparableValue<Value>) _toComparable(resolvableValue.evaluate(processInstance));
    }

    @Override
    public int compareTo(ComparableValue<Boolean> o) {
      throw new IllegalArgumentException("shouldn't be invoked");
    }
  }
}
