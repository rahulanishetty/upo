/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.distributed;

import com.upo.orchestrator.engine.models.ProcessVariable;
import com.upo.orchestrator.engine.services.VariableStore;
import com.upo.resource.redis.RedisTemplateFactory;
import com.upo.resource.redis.impl.JsonRedisCodec;
import com.upo.resource.redis.impl.JsonRepositoryServiceImpl;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Named("DistributedVariableStoreImpl")
@Singleton
public class VariableStoreImpl extends JsonRepositoryServiceImpl<ProcessVariable, String>
    implements VariableStore {

  @Singleton
  public VariableStoreImpl(RedisTemplateFactory redisTemplateFactory) {
    super(
        redisTemplateFactory,
        Resources.REDIS,
        ProcessVariable.class,
        JsonRedisCodec.forStringKey(ProcessVariable.class, ProcessVariable::getId));
  }
}
