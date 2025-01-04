/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.api;

/**
 * A filter condition that evaluates data based on specified criteria. Filters can be used for data
 * comparison, pattern matching, and logical operations. They support both simple comparisons and
 * complex compositions through logical operators.
 */
public interface Filter {

  /**
   * Returns the type of filter operation to be performed. Supported filter types are:
   *
   * <ul>
   *   <li>{@code IN} - Checks if a value exists in a collection
   *   <li>{@code EQUALS} - Exact equality comparison
   *   <li>{@code GT} - Greater than comparison
   *   <li>{@code LT} - Less than comparison
   *   <li>{@code GTE} - Greater than or equal comparison
   *   <li>{@code LTE} - Less than or equal comparison
   *   <li>{@code REGEX} - Regular expression matching (case sensitive)
   *   <li>{@code CONTAINS} - Case-sensitive substring or element containment check
   *   <li>{@code ICONTAINS} - Case-insensitive substring or element containment check
   *   <li>{@code IREGEX} - Case-insensitive regular expression matching
   *   <li>{@code AND} - Logical AND of multiple filter conditions
   *   <li>{@code OR} - Logical OR of multiple filter conditions
   *   <li>{@code NOT} - Logical negation of a filter condition
   * </ul>
   *
   * @return the filter type identifier
   */
  String getType();
}
