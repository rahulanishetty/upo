/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.distributed;

import java.util.*;

import com.upo.orchestrator.engine.ProcessFlowStatus;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.ProcessInstanceStore;
import com.upo.resource.redis.RedisTemplate;
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
  public boolean save(ProcessInstance processInstance, ProcessFlowStatus expectedStatus) {
    return updateIf(processInstance, ProcessInstance.STATUS, expectedStatus.name(), true)
        .isPresent();
  }

  @Override
  public Optional<ProcessInstance> findById(String id, ProcessFlowStatus expectedStatus) {
    return findById(id)
        .filter(processInstance -> Objects.equals(processInstance.getStatus(), expectedStatus));
  }

  @Override
  public boolean deleteById(String processInstanceId) {
    return getRawTemplate()
            .deleteMany(List.of(toKey(processInstanceId), "waitOnChildren/" + processInstanceId))
        > 0;
  }

  @Override
  public void addWaitingOnInstanceIds(
      ProcessInstance parentInstance, Collection<String> waitOnInstanceIds) {
    RedisTemplate rawTemplate = getRawTemplate();
    rawTemplate.addToSet(
        "waitOnChildren/" + parentInstance.getId(), waitOnInstanceIds.toArray(new String[0]));
  }

  @Override
  public boolean removeCompletedInstanceId(
      ProcessInstance parentInstance, String completedInstanceId) {
    RedisTemplate rawTemplate = getRawTemplate();
    return rawTemplate.removeFromSet(
            "waitOnChildren/" + parentInstance.getId(), completedInstanceId)
        > 0;
  }

  @Override
  public Set<String> getRemainingChildren(ProcessInstance processInstance) {
    RedisTemplate rawTemplate = getRawTemplate();
    return rawTemplate.getSetMembers("waitOnChildren/" + processInstance.getId());
  }
}
