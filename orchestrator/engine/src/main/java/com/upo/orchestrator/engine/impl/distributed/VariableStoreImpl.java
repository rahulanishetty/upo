/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.distributed;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.models.ProcessVariable;
import com.upo.orchestrator.engine.services.VariableStore;
import com.upo.resource.redis.RedisTemplateFactory;
import com.upo.resource.redis.impl.JsonRedisCodec;
import com.upo.resource.redis.impl.JsonRepositoryServiceImpl;
import com.upo.utilities.ds.CollectionUtils;

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

  @Override
  public boolean save(ProcessVariable obj) {
    boolean saved = super.save(obj);
    if (saved) {
      getRawTemplate().addToSet("byInstance/" + obj.getProcessInstanceId(), obj.getId());
    }
    return saved;
  }

  @Override
  public boolean saveMany(Collection<ProcessVariable> objects) {
    boolean saved = super.saveMany(objects);
    if (saved) {
      Map<String, List<String>> byInstanceIds =
          CollectionUtils.groupByKey(
              objects, ProcessVariable::getProcessInstanceId, ProcessVariable::getId);
      if (CollectionUtils.isNotEmpty(byInstanceIds)) {
        for (Map.Entry<String, List<String>> entry : byInstanceIds.entrySet()) {
          getRawTemplate()
              .addToSet("byInstance/" + entry.getKey(), entry.getValue().toArray(new String[0]));
        }
      }
    }
    return saved;
  }

  @Override
  public Collection<Variable> findVariablesForInstance(ProcessInstance processInstance) {
    Set<String> members = getRawTemplate().getSetMembers("byInstance/" + processInstance.getId());
    return CollectionUtils.transformToList(
        findByIds(members).values(), processVariable -> processVariable);
  }
}
