/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.api;

/** Case-insensitive substring containment filter. */
public class InsensitiveContainsFilter extends TextMatchFilter {
  public InsensitiveContainsFilter(String field, String pattern) {
    super(field, pattern);
  }

  @Override
  public String getType() {
    return "ICONTAINS";
  }
}
