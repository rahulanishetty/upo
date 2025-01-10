/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;

public class RedisClient implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(RedisClient.class);

  private final Vertx vertx;
  private final io.vertx.redis.client.Redis redis;

  public RedisClient(RedisOptions redisOptions) {
    this.vertx = Vertx.vertx();
    this.redis = Redis.createClient(vertx, redisOptions);
  }

  public Redis getRedis() {
    return redis;
  }

  @Override
  public void close() throws IOException {
    vertx.close(
        result -> {
          if (result.failed()) {
            LOGGER.error("failed to close vertx client", result.cause());
          }
        });
  }
}
