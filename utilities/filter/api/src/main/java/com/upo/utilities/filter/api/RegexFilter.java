/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.api;

/** Regular expression matching filter (case sensitive). */
public class RegexFilter extends TextMatchFilter {
  public RegexFilter(String field, String pattern) {
    super(field, pattern);
  }

  @Override
  public String getType() {
    return "REGEX";
  }
}
