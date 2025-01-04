/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.api;

/** Logical NOT operation on a filter. */
public class NotFilter implements Filter {
  private final Filter filter;

  public NotFilter(Filter filter) {
    this.filter = filter;
  }

  @Override
  public String getType() {
    return "NOT";
  }

  public Filter getFilter() {
    return filter;
  }
}
