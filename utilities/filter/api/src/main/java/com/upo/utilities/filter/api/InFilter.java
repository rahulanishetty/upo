/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.api;

import java.util.Collection;

/** A filter that checks if a value exists in a collection. */
public class InFilter implements Filter {

  private final String field;
  private final Collection<?> values;

  public InFilter(String field, Collection<?> values) {
    this.field = field;
    this.values = values;
  }

  @Override
  public String getType() {
    return "IN";
  }

  public String getField() {
    return field;
  }

  public Collection<?> getValues() {
    return values;
  }
}
