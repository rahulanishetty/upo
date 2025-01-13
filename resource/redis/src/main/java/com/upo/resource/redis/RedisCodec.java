/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

public interface RedisCodec<T, ID> {

  String toString(T obj);

  String serializeId(ID id);

  ID deserializeId(String id);

  T fromString(String value);

  String getId(T obj);
}
