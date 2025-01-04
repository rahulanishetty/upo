/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl;

/**
 * Provides context for resolving and accessing fields within a record type. This interface acts as
 * a field resolver, converting string-based field references into strongly-typed field accessors.
 *
 * @param <Record> the type of record whose fields can be resolved by this context
 */
public interface FilterContext<Record> {

  /**
   * Resolves a field name into a typed Field accessor.
   *
   * @param field the name of the field to resolve
   * @return a Field accessor that can extract and convert values from the Record type
   */
  Field<Record> resolveField(String field);
}
