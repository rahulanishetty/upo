/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.distributed;

import java.util.Optional;

import com.upo.orchestrator.engine.impl.AbstractExecutionLifecycleManager;
import com.upo.orchestrator.engine.impl.events.LifecycleEvent;
import com.upo.orchestrator.engine.services.EnvironmentProvider;
import com.upo.resource.redis.RedisTemplate;
import com.upo.resource.redis.RedisTemplateFactory;
import com.upo.utilities.json.Utils;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * Distributed implementation of ExecutionLifecycleManager using Redis for event distribution.
 * Events are partitioned and stored in Redis lists for processing by consumers.
 */
@Named("DistributedExecutionLifecycleManagerImpl")
@Singleton
public class ExecutionLifecycleManagerImpl extends AbstractExecutionLifecycleManager {

  private static final int DEFAULT_PARTITIONS = 8;

  private final RedisTemplateFactory redisTemplateFactory;

  @Inject
  public ExecutionLifecycleManagerImpl(RedisTemplateFactory redisTemplateFactory) {
    this.redisTemplateFactory = redisTemplateFactory;
  }

  @Override
  protected void handleEvent(LifecycleEvent lifecycleEvent) {
    if (lifecycleEvent == null) {
      return;
    }
    String eventKey = createEventKey(lifecycleEvent);
    String eventJson = Utils.toJson(lifecycleEvent);

    getRedisTemplate().addToList(eventKey, eventJson);
  }

  private String createEventKey(LifecycleEvent lifecycleEvent) {
    return String.format("%s/%d", lifecycleEvent.getType(), calculatePartition(lifecycleEvent));
  }

  private int calculatePartition(LifecycleEvent lifecycleEvent) {
    return Math.abs(lifecycleEvent.getPartitionKey().hashCode()) % getMaxPartitions();
  }

  private RedisTemplate getRedisTemplate() {
    return redisTemplateFactory.getRedisTemplate(
        Resources.REDIS, EnvironmentProvider.getCurrentTier());
  }

  private int getMaxPartitions() {
    return Optional.ofNullable(System.getenv("MAX_PARTITIONS"))
        .filter(s -> !s.isEmpty())
        .map(Integer::parseInt)
        .filter(p -> p > 0)
        .orElse(DEFAULT_PARTITIONS);
  }
}
