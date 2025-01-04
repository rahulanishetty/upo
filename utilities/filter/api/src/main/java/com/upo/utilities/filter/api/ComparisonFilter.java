/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.api;

/** Base class for comparison filters (GT, LT, GTE, LTE). */
public abstract class ComparisonFilter implements Filter {
  private final String field;
  private final Comparable<?> value;

  protected ComparisonFilter(String field, Comparable<?> value) {
    this.field = field;
    this.value = value;
  }

  public String getField() {
    return field;
  }

  public Comparable<?> getValue() {
    return value;
  }
}
