/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.api;

/** Filter that checks if a field is missing (has null value) in a record. */
public class MissingFilter implements Filter {
  public static final String TYPE = "MISSING";
  private final String field;

  public MissingFilter(String field) {
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
