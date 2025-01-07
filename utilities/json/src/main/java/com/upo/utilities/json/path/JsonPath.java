/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.json.path;

public interface JsonPath {

  Object read(Object object);

  static JsonPath create(String path) {
    return new DotNotationJsonPath(path);
  }
}
