/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.distributed;

import java.util.Objects;
import java.util.Optional;

import com.upo.orchestrator.engine.TaskResult;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.ProcessInstanceStore;
import com.upo.resource.redis.RedisTemplateFactory;
import com.upo.resource.redis.impl.JsonRedisCodec;
import com.upo.resource.redis.impl.JsonRepositoryServiceImpl;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Named("DistributedProcessInstanceStoreImpl")
@Singleton
public class ProcessInstanceStoreImpl extends JsonRepositoryServiceImpl<ProcessInstance, String>
    implements ProcessInstanceStore {

  @Inject
  public ProcessInstanceStoreImpl(RedisTemplateFactory redisTemplateFactory) {
    super(
        redisTemplateFactory,
        Resources.REDIS,
        ProcessInstance.class,
        JsonRedisCodec.forStringKey(ProcessInstance.class, ProcessInstance::getId));
  }

  @Override
  public boolean save(ProcessInstance processInstance, TaskResult.Status expectedStatus) {
    return updateIf(processInstance, ProcessInstance.STATUS, expectedStatus.name(), true)
        .isPresent();
  }

  @Override
  public Optional<ProcessInstance> findById(String id, TaskResult.Status expectedStatus) {
    return findById(id)
        .filter(processInstance -> Objects.equals(processInstance.getStatus(), expectedStatus));
  }
}
