/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import java.util.Optional;

public interface JsonRepositoryService<T, ID> extends RepositoryService<T, ID> {
  /**
   * Updates an entity if the value at the specified field matches the expected value.
   *
   * @param obj Entity to update
   * @param field Path to the field to compare (e.g., "metadata.version")
   * @param expectedValue Expected value at the path
   * @param returnOld If true, returns old value; if false, returns new value
   * @return UpdateResult containing operation status and either old or new value
   */
  Optional<T> updateIf(T obj, String field, String expectedValue, boolean returnOld);
}
