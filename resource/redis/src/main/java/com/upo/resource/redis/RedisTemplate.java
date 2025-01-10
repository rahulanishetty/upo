/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

public class RedisTemplate {

  private final RedisClient redisClient;
  private final String prefix;

  public RedisTemplate(RedisClient redisClient, String prefix) {
    this.redisClient = redisClient;
    this.prefix = prefix;
  }
}
