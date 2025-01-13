/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis.impl;

import java.util.function.Function;

import com.upo.resource.redis.RedisCodec;
import com.upo.utilities.json.Utils;

public abstract class JsonRedisCodec<T, ID> implements RedisCodec<T, ID> {

  private final Class<T> type;
  private final Function<T, ID> idProvider;

  public JsonRedisCodec(Class<T> clz, Function<T, ID> idProvider) {
    this.type = clz;
    this.idProvider = idProvider;
  }

  @Override
  public String toString(T obj) {
    return Utils.toJson(obj);
  }

  @Override
  public T fromString(String value) {
    return Utils.fromJson(value, this.type);
  }

  @Override
  public String getId(T obj) {
    return serializeId(idProvider.apply(obj));
  }

  public static <T> JsonRedisCodec<T, String> forStringKey(
      Class<T> clz, Function<T, String> idExtractor) {
    return new JsonRedisCodec<>(clz, idExtractor) {
      @Override
      public String serializeId(String id) {
        return id;
      }

      @Override
      public String deserializeId(String id) {
        return id;
      }
    };
  }

  public static <T> JsonRedisCodec<T, Long> forLongKey(
      Class<T> clz, Function<T, Long> idExtractor) {
    return new JsonRedisCodec<>(clz, idExtractor) {
      @Override
      public String serializeId(Long id) {
        return Long.toString(id);
      }

      @Override
      public Long deserializeId(String id) {
        return Long.parseLong(id);
      }
    };
  }
}
