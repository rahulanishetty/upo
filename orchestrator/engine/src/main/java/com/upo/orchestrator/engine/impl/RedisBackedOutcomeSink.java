/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import com.alibaba.fastjson2.annotation.JSONType;
import com.upo.orchestrator.engine.ProcessOutcome;
import com.upo.orchestrator.engine.ProcessOutcomeSink;
import com.upo.orchestrator.engine.ProcessServices;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.resource.redis.RedisTemplate;
import com.upo.resource.redis.RedisTemplateFactory;
import com.upo.utilities.json.Utils;

@JSONType(typeName = "REDIS")
public class RedisBackedOutcomeSink extends ProcessOutcomeSink {

  private String resourceType;
  private String partitionKey;
  private String channelName;

  public RedisBackedOutcomeSink() {
    super("REDIS");
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getPartitionKey() {
    return partitionKey;
  }

  public void setPartitionKey(String partitionKey) {
    this.partitionKey = partitionKey;
  }

  public String getChannelName() {
    return channelName;
  }

  public void setChannelName(String channelName) {
    this.channelName = channelName;
  }

  @Override
  public void onOutcome(ProcessInstance processInstance, ProcessOutcome outcome) {
    ProcessServices processServices = processInstance.getProcessEnv().getProcessServices();
    RedisTemplateFactory redisTemplateFactory =
        processServices.getService(RedisTemplateFactory.class);
    RedisTemplate redisTemplate =
        redisTemplateFactory.getRedisTemplate(() -> resourceType, partitionKey);
    redisTemplate.publish(channelName, Utils.toJson(outcome));
  }

  @Override
  public boolean isSerializable() {
    return true;
  }
}
