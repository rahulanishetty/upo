/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.api;

/** Less than or equal comparison filter. */
public class LessThanEqualsFilter extends ComparisonFilter {
  public static final String TYPE = "LTE";

  public LessThanEqualsFilter(String field, Comparable<?> value) {
    super(field, value);
  }

  @Override
  public String getType() {
    return TYPE;
  }
}
