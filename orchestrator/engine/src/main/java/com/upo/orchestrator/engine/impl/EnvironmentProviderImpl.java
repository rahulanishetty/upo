/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.upo.orchestrator.engine.impl.distributed.Resources;
import com.upo.orchestrator.engine.services.EnvironmentProvider;
import com.upo.resource.redis.RedisTemplate;
import com.upo.resource.redis.RedisTemplateFactory;
import com.upo.utilities.context.RequestContext;
import com.upo.utilities.json.Utils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Provides environment configuration from Redis, supporting tier-specific overrides. Environment
 * variables are loaded in order: 1. Tier-specific configuration (ENV/{tier}) 2. Default
 * configuration (ENV)
 */
@Singleton
public class EnvironmentProviderImpl implements EnvironmentProvider {

  private final RedisTemplateFactory redisTemplateFactory;

  @Inject
  public EnvironmentProviderImpl(RedisTemplateFactory redisTemplateFactory) {
    this.redisTemplateFactory = redisTemplateFactory;
  }

  @Override
  public Map<String, Object> lookupEnvVariables() {
    Map<String, Object> env = new HashMap<>();

   // Load tier-specific config
    String currentTier = EnvironmentProvider.getCurrentTier();
    Map<String, Object> tierConfig = loadTierConfig(currentTier);
    if (tierConfig != null) {
      env.putAll(tierConfig);
    }

   // Load default config as fallback
    Map<String, Object> defaultConfig = loadTierConfig(null);
    if (defaultConfig != null) {
      defaultConfig.forEach(env::putIfAbsent);
    }

    return env;
  }

  @Override
  public boolean isShutdownInProgress() {
    return false;
  }

  private Map<String, Object> loadTierConfig(String tier) {
    RedisTemplate redisTemplate = getRedisTemplate();
    Optional<String> envConfig = redisTemplate.get(buildEnvKey(tier));
    return envConfig
        .<Map<String, Object>>map(s -> Utils.fromJson(s, Utils.GENERIC_JSON_MAP_TYPE))
        .orElse(null);
  }

  private static String buildEnvKey(String tier) {
    return tier == null || tier.isEmpty() ? "ENV" : "ENV/" + tier;
  }

  private RedisTemplate getRedisTemplate() {
    RequestContext requestContext = RequestContext.get();
    if (requestContext == null) {
      throw new IllegalStateException("request context not set!");
    }
    return redisTemplateFactory.getRedisTemplate(Resources.REDIS, requestContext.getPartitionKey());
  }
}
