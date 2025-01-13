/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.upo.resource.client.base.models.ResourceType;
import com.upo.resource.redis.impl.JsonRedisCodec;
import com.upo.utilities.ds.CollectionUtils;

import io.lettuce.core.RedisNoScriptException;
import io.lettuce.core.ScriptOutputType;

/**
 * JSON-specific implementation of RepositoryService that supports conditional updates based on JSON
 * field values.
 *
 * @param <T> Type of the entity
 * @param <ID> Type of the entity's identifier
 */
public class JsonRepositoryServiceImpl<T, ID> extends RepositoryServiceImpl<T, ID>
    implements JsonRepositoryService<T, ID> {

  public JsonRepositoryServiceImpl(
      RedisTemplateFactory redisTemplateFactory,
      ResourceType resourceType,
      Class<T> clz,
      JsonRedisCodec<T, ID> codec) {
    super(redisTemplateFactory, resourceType, clz, codec);
  }

  @Override
  public Optional<T> updateIf(T obj, String field, String expectedValue, boolean returnOld) {
    List<Object> result =
        executeScript(
            StandardScripts.UPDATE_IF,
            Collections.singletonList(createKey(obj)),
            Arrays.asList(toString(obj), field, expectedValue, String.valueOf(returnOld)));
    return processUpdateResult(result);
  }

  private Optional<T> processUpdateResult(List<Object> result) {
    if (result != null && result.size() > 1) {
      String value = (String) result.get(0);
      Long updateStatus = (Long) result.get(1);
      boolean success = updateStatus == 1L;
      if (success) {
        T entity = value != null ? fromString(value) : null;
        return Optional.ofNullable(entity);
      }
    }
    return Optional.empty();
  }

  private List<Object> executeScript(String scriptId, List<String> keys, List<String> args) {
    RedisTemplate redisTemplate = getRedisTemplate();
    try (RedisCommands commands = getRedisCommands(redisTemplate)) {
      String keyNamespace = redisTemplate.getKeyNamespace();
      String[] keysWithNamespace =
          CollectionUtils.transformToArray(keys, String[]::new, key -> keyNamespace + key);
      try {
        String digest = ensureScriptRegistered(scriptId, redisTemplate, false);
        return _executeScript(digest, args, commands, keysWithNamespace);
      } catch (RedisNoScriptException eX) {
        String digest = ensureScriptRegistered(scriptId, redisTemplate, true);
        return _executeScript(digest, args, commands, keysWithNamespace);
      }
    }
  }

  private List<Object> _executeScript(
      String sha, List<String> args, RedisCommands commands, String[] keysWithNamespace) {
    return commands.evalsha(
        sha, ScriptOutputType.MULTI, keysWithNamespace, args.toArray(new String[0]));
  }

  private String ensureScriptRegistered(
      String scriptId, RedisTemplate redisTemplate, boolean force) {
    return redisTemplate.loadStandardScript(scriptId, force);
  }

  public RedisCommands getRedisCommands(RedisTemplate redisTemplate) {
    if (redisTemplate instanceof WithRedisCommands withRedisCommands) {
      return withRedisCommands.getRedisCommands();
    }
    throw new IllegalStateException("RedisTemplate doesn't implement with redis commands");
  }
}
