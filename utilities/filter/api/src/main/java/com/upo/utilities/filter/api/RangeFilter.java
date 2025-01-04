/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.api;

/**
 * Filter that checks if a field's value falls within a specified range. Supports both inclusive and
 * exclusive bounds on either end.
 */
public class RangeFilter implements Filter {
  public static final String TYPE = "RANGE";

  private final String field;
  private final Object from;
  private final Object to;
  private final boolean fromInclusive;
  private final boolean toInclusive;

  public RangeFilter(
      String field, Object from, Object to, boolean fromInclusive, boolean toInclusive) {
    this.field = field;
    this.from = from;
    this.to = to;
    this.fromInclusive = fromInclusive;
    this.toInclusive = toInclusive;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  public String getField() {
    return field;
  }

  public Object getFrom() {
    return from;
  }

  public Object getTo() {
    return to;
  }

  public boolean isFromInclusive() {
    return fromInclusive;
  }

  public boolean isToInclusive() {
    return toInclusive;
  }
}
