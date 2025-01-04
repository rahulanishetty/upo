/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl;

import java.util.Collection;

/**
 * Specialized field type that supports nested object structures with their own contexts. This
 * interface extends Field to provide support for resolving fields within nested objects or arrays,
 * maintaining proper type information and context at each nesting level.
 *
 * @param <Type> the type of the parent object containing this field
 */
public interface NestedField<Type> extends Field<Type> {

  @Override
  default Collection<ComparableValue<?>> resolveValues(Type value) {
    throw new IllegalStateException("shouldn't be called");
  }

  <InnerType> Collection<InnerType> resolveInnerObjects(Type value);

  /**
   * Creates a context for handling fields within the nested objects. This method provides type-safe
   * access to fields of nested objects, allowing proper field resolution within the nested
   * structure.
   *
   * @param filterContext the parent context, useful for maintaining context chain
   * @param <InnerType> the type of objects within the nested structure
   * @return a context for resolving fields within the nested objects
   */
  <InnerType> FilterContext<InnerType> getInnerContext(FilterContext<Type> filterContext);
}
