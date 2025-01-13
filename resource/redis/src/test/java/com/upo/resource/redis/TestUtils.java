/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import java.util.Collections;

import com.upo.resource.client.base.TestResourceConfigProvider;
import com.upo.resource.redis.models.HostPort;
import com.upo.resource.redis.models.RedisServerConfig;
import com.upo.resource.redis.models.RedisTemplateResourceConfig;

public class TestUtils {

  public static void registerRedisServerConfig(
      String host, int port, TestResourceConfigProvider testResourceConfigProvider) {
    RedisServerConfig redisServerConfig = new RedisServerConfig();
    redisServerConfig.setId("REDIS_SERVER/REDIS_SERVER/redis-1");
    redisServerConfig.setResourceCategory("REDIS_SERVER");
    redisServerConfig.setResourceType("REDIS_SERVER");
    HostPort hostPort = new HostPort();
    hostPort.setHost(host);
    hostPort.setPort(port);
    redisServerConfig.setHostPorts(Collections.singletonList(hostPort));
    redisServerConfig.setSsl(false);
    redisServerConfig.setClientType(RedisServerConfig.ClientType.STANDALONE);
    testResourceConfigProvider.registerResource(redisServerConfig);
  }

  public static void registerRedisTemplateResourceConfig(
      String resourceType,
      String partitionKey,
      TestResourceConfigProvider testResourceConfigProvider) {
    RedisTemplateResourceConfig templateConfig = new RedisTemplateResourceConfig();
    templateConfig.setId("REDIS_SERVER/" + resourceType + "/" + partitionKey);
    templateConfig.setResourceCategory("REDIS_SERVER");
    templateConfig.setResourceType(resourceType);
    templateConfig.setPrefix("TEST");
    templateConfig.setResourceIdSuffix("redis-1");
    templateConfig.setPartitionKey(partitionKey);
    testResourceConfigProvider.registerResource(templateConfig);
  }
}
