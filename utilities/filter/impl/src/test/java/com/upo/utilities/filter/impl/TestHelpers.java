/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl;

import java.util.*;

public class TestHelpers {

  public static class TestRecord {
    private final Map<String, Object> values = new HashMap<>();

    public TestRecord put(String field, Object value) {
      values.put(field, value);
      return this;
    }

    public Object get(String field) {
      return values.get(field);
    }
  }

  public static class TestNestedField<Type> implements NestedField<Type> {
    private final String fieldName;

    public TestNestedField(String fieldName) {
      this.fieldName = fieldName;
    }

    @Override
    public ComparableValue<?> toComparable(Object value) {
      throw new IllegalArgumentException("this shouldn't be called");
    }

    @Override
    public <InnerType> Collection<InnerType> resolveInnerObjects(Type value) {
      if (!(value instanceof TestRecord)) {
        throw new IllegalArgumentException("Expected TestRecord");
      }
      Object fieldValue = ((TestRecord) value).get(fieldName);
      if (fieldValue == null) {
        return Collections.emptyList();
      }
      if (fieldValue instanceof Collection<?> collection) {
       //noinspection unchecked
        return (Collection<InnerType>) collection;
      }
     //noinspection unchecked
      return (Collection<InnerType>) Collections.singletonList(fieldValue);
    }

    @Override
    public <InnerType> FilterContext<InnerType> getInnerContext(FilterContext<Type> filterContext) {
     //noinspection unchecked
      return (FilterContext<InnerType>) filterContext;
    }
  }

  public static class TestField<Type> implements Field<Type> {
    private final String fieldName;

    TestField(String fieldName) {
      this.fieldName = fieldName;
    }

    @Override
    public ComparableValue<?> toComparable(Object value) {
     //noinspection rawtypes,unchecked
      return new TestComparableValue<>((Comparable) value);
    }

    @Override
    public Collection<ComparableValue<?>> resolveValues(Type value) {
      if (!(value instanceof TestRecord)) {
        throw new IllegalArgumentException("Expected TestRecord");
      }
      Object fieldValue = ((TestRecord) value).get(fieldName);
      if (fieldValue == null) {
        return Collections.emptyList();
      }
     //noinspection rawtypes,unchecked
      return Collections.singletonList(new TestComparableValue<>((Comparable) fieldValue));
    }
  }

  public static class TestComparableValue<T extends Comparable<T>> implements ComparableValue<T> {
    private final T value;

    TestComparableValue(T value) {
      this.value = value;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(ComparableValue<T> other) {
      if (!(other instanceof TestComparableValue)) {
        throw new IllegalArgumentException("Can only compare with TestComparableValue");
      }
      return value.compareTo(((TestComparableValue<T>) other).value);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof TestComparableValue<?> other)) {
        return false;
      }
      return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  public static class TestFilterContext<Type> implements FilterContext<Type> {
    @Override
    public Field<Type> resolveField(String field) {
      return new TestField<>(field);
    }
  }

  public static class TestWithNestedFilterContext<Type> extends TestFilterContext<Type> {
    @Override
    public Field<Type> resolveField(String field) {
      if (Objects.equals("items", field)) {
        return new TestNestedField<>(field);
      }
      return super.resolveField(field);
    }
  }
}
