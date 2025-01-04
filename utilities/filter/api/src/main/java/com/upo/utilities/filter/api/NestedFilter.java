/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.api;

import java.util.List;

/**
 * A filter that evaluates conditions on array elements while preserving the context of each
 * individual object. This is similar to nested queries in document databases, where you need to
 * match conditions against multiple fields of the same array element.
 *
 * <p>For example, if you have an array of items: { "items": [ {"price": 100, "quantity": 5},
 * {"price": 200, "quantity": 2} ] }
 *
 * <p>You can use NestedFilter to find items where price > 150 AND quantity < 3, ensuring both
 * conditions match on the same array element.
 */
public class NestedFilter implements Filter {
  private final String arrayField;// field containing the array
  private final List<Filter> filters;// filters to apply on array elements

  public NestedFilter(String arrayField, List<Filter> filters) {
    this.arrayField = arrayField;
    this.filters = filters;
  }

  @Override
  public String getType() {
    return "NESTED";
  }

  public String getArrayField() {
    return arrayField;
  }

  public List<Filter> getFilters() {
    return filters;
  }
}
