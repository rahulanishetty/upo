/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.ds;

/** Utility class providing helper methods for resource management and I/O operations. */
public class IOUtils {
  /**
   * Closes one or more {@link AutoCloseable} resources without throwing exceptions.
   *
   * @param list Variable number of {@link AutoCloseable} resources to close
   */
  public static void closeQuietly(AutoCloseable... list) {
    for (AutoCloseable closeable : list) {
      try {
        closeable.close();
      } catch (Throwable th) {
       // ignore
      }
    }
  }
}
