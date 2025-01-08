/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.json.path;

/** Interface for accessing data using path expressions. */
public interface JsonPath {

  /**
   * Reads the value at this path from the given object.
   *
   * @param object The root object to read from
   * @return The value at the path, or null if not found
   * @throws RuntimeException if path is invalid or data structure doesn't match path
   */
  Object read(Object object);

  /**
   * Returns the token at the specified index in the path. For a path like
   * "customer.addresses[1].city": - getToken(0) returns "customer" - getToken(1) returns
   * "addresses" - getToken(2) returns "city"
   *
   * <p>For a path with array notation as part of key like "customer.addresses.1.city": -
   * getToken(2) returns "1"
   *
   * @param index The zero-based index of the token to retrieve
   * @return The token at the specified index
   * @throws IndexOutOfBoundsException if index is negative or exceeds path length
   */
  String getToken(int index);

  /**
   * Creates a new JsonPath instance for the given path expression.
   *
   * @param path The path expression (e.g. "customer.addresses[1].city")
   * @return A new JsonPath instance
   * @throws IllegalArgumentException if path syntax is invalid
   */
  static JsonPath create(String path) {
    return new DotNotationJsonPath(path);
  }
}
