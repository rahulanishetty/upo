/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.api;

import java.util.List;

/** Logical OR combination of multiple filters. */
public class OrFilter implements Filter {
  public static final String TYPE = "OR";
  private final List<Filter> filters;

  public OrFilter(List<Filter> filters) {
    this.filters = filters;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  public List<Filter> getFilters() {
    return filters;
  }
}
