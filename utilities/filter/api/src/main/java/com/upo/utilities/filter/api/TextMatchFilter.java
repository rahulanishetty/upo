/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.api;

/** Base class for text matching filters (REGEX, CONTAINS, ICONTAINS, IREGEX). */
public abstract class TextMatchFilter implements Filter {
  private final String field;
  private final String pattern;

  protected TextMatchFilter(String field, String pattern) {
    this.field = field;
    this.pattern = pattern;
  }

  public String getField() {
    return field;
  }

  public String getPattern() {
    return pattern;
  }
}
