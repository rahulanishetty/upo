/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.api;

/** Filter that checks if a field exists (has non-null value) in a record. */
public class ExistsFilter implements Filter {
  public static final String TYPE = "EXISTS";
  private final String field;

  public ExistsFilter(String field) {
    this.field = field;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  public String getField() {
    return field;
  }
}
