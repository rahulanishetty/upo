/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import com.upo.resource.client.base.ResourceConfigProvider;
import com.upo.resource.client.base.impl.ResourceTemplateFactoryImpl;
import com.upo.resource.client.base.models.ResourceType;
import com.upo.resource.redis.models.RedisServerConfig;
import com.upo.resource.redis.models.RedisTemplateResourceConfig;

import io.vertx.core.net.NetClientOptions;
import io.vertx.redis.client.RedisClientType;
import io.vertx.redis.client.RedisOptions;
import io.vertx.redis.client.RedisReplicas;
import io.vertx.redis.client.RedisRole;

public class RedisTemplateFactoryImpl
    extends ResourceTemplateFactoryImpl<
        RedisTemplate, RedisClient, RedisTemplateResourceConfig, RedisServerConfig>
    implements RedisTemplateFactory {

  public RedisTemplateFactoryImpl(ResourceConfigProvider resourceConfigProvider) {
    super(
        resourceConfigProvider,
        () -> "REDIS_SERVER",
        RedisServerConfig.class,
        RedisTemplateResourceConfig.class);
  }

  @Override
  protected RedisTemplate createTemplate(
      RedisClient redisClient, RedisTemplateResourceConfig config) {
    String prefix = config.getPrefix();
    if (prefix == null || prefix.isEmpty()) {
      prefix = "";
    }
    prefix += "/" + config.getPartitionKey() + "/" + config.getResourceType();
    return new RedisTemplate(redisClient, prefix);
  }

  @Override
  protected RedisClient createClient(RedisServerConfig config) {
    return new RedisClient(createRedisOptions(config));
  }

  @Override
  public RedisTemplate getRedisTemplate(ResourceType resourceType, String partitionKey) {
    return getTemplateOrFail(resourceType, partitionKey);
  }

  static RedisOptions createRedisOptions(RedisServerConfig config) {
    RedisOptions redisOptions = new RedisOptions();
    String connectionString = "redis://" + config.getHost() + ":" + config.getPort();
    redisOptions.setConnectionString(connectionString);
    if (config.isSsl()) {
      NetClientOptions netClientOptions = redisOptions.getNetClientOptions();
      netClientOptions.setSsl(true);
      if (config.isDisableHostNameVerification()) {
        netClientOptions.setHostnameVerificationAlgorithm("");
      } else {
        netClientOptions.setHostnameVerificationAlgorithm("HTTPS");
      }
    }
    if (config.getClientType() != null) {
      redisOptions.setType(RedisClientType.valueOf(config.getClientType()));
    }
    if (config.getRole() != null) {
      redisOptions.setRole(RedisRole.valueOf(config.getRole()));
    }
    if (config.getUseReplicas() != null) {
      redisOptions.setUseReplicas(RedisReplicas.valueOf(config.getUseReplicas()));
    }
    redisOptions.setMasterName(config.getMasterName());
    redisOptions.setPassword(config.getPassword());

    if (config.getPoolCleanerInterval() != null) {
      redisOptions.setPoolCleanerInterval(config.getPoolCleanerInterval());
    }
    if (config.getMaxPoolSize() != null) {
      redisOptions.setMaxPoolSize(config.getMaxPoolSize());
    }
    if (config.getPoolMaxWaiting() != null) {
      redisOptions.setMaxPoolWaiting(config.getPoolMaxWaiting());
    }
    if (config.getPoolRecycleTimeout() != null) {
      redisOptions.setPoolRecycleTimeout(config.getPoolRecycleTimeout());
    }

    if (config.getMaxWaitingHandlers() != null) {
      redisOptions.setMaxWaitingHandlers(config.getMaxWaitingHandlers());
    }
    if (config.getMaxNestedArrays() != null) {
      redisOptions.setMaxNestedArrays(config.getMaxNestedArrays());
    }
    return redisOptions;
  }
}
