/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.api;

/** A filter that checks for exact equality between values. */
public class EqualsFilter implements Filter {
  public static final String TYPE = "EQUALS";

  private final String field;
  private final Object value;

  public EqualsFilter(String field, Object value) {
    this.field = field;
    this.value = value;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  public String getField() {
    return field;
  }

  public Object getValue() {
    return value;
  }
}
