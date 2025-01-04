/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.api;

/**
 * Filter that matches no records (always returns false). Useful as a default rejection filter or in
 * logical operations.
 */
public class MatchNoneFilter implements Filter {

  public static final String TYPE = "MATCH_NONE";

  @Override
  public String getType() {
    return TYPE;
  }
}
