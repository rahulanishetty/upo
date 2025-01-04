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
 * Represents a field within a record type, providing methods to access and convert field values
 * into comparable formats.
 *
 * @param <Type> the type of record this field belongs to
 */
public interface Field<Type> {

  /**
   * Converts a raw value into a comparable format for this field. This method is typically used
   * when converting filter condition values into a format that can be compared with actual field
   * values.
   *
   * @param value the raw value to convert
   * @return the value wrapped in a comparable format
   */
  ComparableValue<?> toComparable(Object value);

  /**
   * Resolves the field values from a record instance. This method handles extraction of field
   * values, including cases where a field might contain multiple values (e.g., arrays or
   * collections).
   *
   * @param value the record instance to extract values from
   * @return collection of field values in comparable format
   */
  Collection<ComparableValue<?>> resolveValues(Type value);
}
