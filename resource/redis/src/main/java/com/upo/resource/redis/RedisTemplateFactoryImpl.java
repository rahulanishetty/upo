/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import static com.upo.resource.redis.models.RedisServerConfig.ClientType.*;

import com.upo.resource.client.base.ResourceConfigProvider;
import com.upo.resource.client.base.impl.ResourceTemplateFactoryImpl;
import com.upo.resource.client.base.models.ResourceType;
import com.upo.resource.redis.models.RedisServerConfig;
import com.upo.resource.redis.models.RedisTemplateResourceConfig;

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
    return new RedisTemplateImpl(redisClient, prefix);
  }

  @Override
  protected RedisClient createClient(RedisServerConfig config) {
    return RedisClient.create(config);
  }

  @Override
  public RedisTemplate getRedisTemplate(ResourceType resourceType, String partitionKey) {
    return getTemplateOrFail(resourceType, partitionKey);
  }
}
