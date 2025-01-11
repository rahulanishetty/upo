/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import com.upo.utilities.json.Utils;

public class RedisTemplateImpl implements RedisTemplate {

  private final RedisClient redisClient;
  private final String prefix;

  public RedisTemplateImpl(RedisClient redisClient, String prefix) {
    this.redisClient = redisClient;
    this.prefix = prefix.endsWith("/") ? prefix : prefix + "/";
  }

  @Override
  public <T> boolean insert(String id, T obj, Class<T> clz) {
    try (var commands = getCommands()) {
      return Boolean.TRUE.equals(commands.setnx(createId(clz, id), toValue(obj)));
    }
  }

  private RedisCommands getCommands() {
    return redisClient.getRedisCommands();
  }

  private <T> String toValue(T obj) {
    if (obj == null) {
      return null;
    }
    return Utils.toJson(obj);
  }

  private <T> String createId(Class<T> clz, String id) {
    return prefix + clz.getSimpleName() + "/" + id;
  }
}
