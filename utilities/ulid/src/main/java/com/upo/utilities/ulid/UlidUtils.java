/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.ulid;

import com.github.f4b6a3.ulid.UlidFactory;

public class UlidUtils {

  private static final UlidFactory INSTANCE = UlidFactory.newInstance();

  public static String createId() {
    return INSTANCE.create().toString();
  }
}
