/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl;

/**
 * Marker interface indicating a type can resolve its value based on a record context. This
 * interface is used for types that require runtime resolution from a record (e.g., process instance
 * variables, context data) rather than containing static values.
 *
 * <p>Implementing types may represent:
 *
 * <ul>
 *   <li>Variable references requiring resolution
 *   <li>Dynamic expressions needing evaluation
 *   <li>Values derived from record state
 * </ul>
 *
 * @param <Record> The type of record used for value resolution
 * @see ComparableValue
 */
public interface RecordAware<Record> {

  /**
   * Resolves this reference using the provided record context.
   *
   * @param record The record context for resolution
   * @return A ComparableValue containing the resolved value
   */
  <Value extends Comparable<Value>> ComparableValue<Value> resolve(Record record);
}
