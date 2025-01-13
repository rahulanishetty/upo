/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import java.util.*;
import java.util.stream.Collectors;

import com.upo.utilities.ds.CollectionUtils;

import io.lettuce.core.GetExArgs;
import io.lettuce.core.KeyValue;
import io.lettuce.core.SetArgs;

public class RedisTemplateImpl implements RedisTemplate, WithRedisCommands {

  private final RedisClient redisClient;
  private final String prefix;

  public RedisTemplateImpl(RedisClient redisClient, String prefix) {
    this.redisClient = redisClient;
    this.prefix = prefix.endsWith("/") ? prefix : prefix + "/";
  }

  @Override
  public boolean insert(String id, String value) {
    try (var commands = getCommands()) {
      return Boolean.TRUE.equals(commands.setnx(createId(id), value));
    }
  }

  @Override
  public boolean insertWithExpiry(String id, String value, long expirySeconds) {
    try (var commands = getCommands()) {
      return "OK".equals(commands.set(createId(id), value, SetArgs.Builder.nx().ex(expirySeconds)));
    }
  }

  @Override
  public boolean insertMany(Map<String, String> entries) {
    try (var commands = getCommands()) {
      Map<String, String> map = new LinkedHashMap<>();
      for (Map.Entry<String, String> entry : entries.entrySet()) {
        map.put(createId(entry.getKey()), entry.getValue());
      }
      return Boolean.TRUE.equals(commands.msetnx(map));
    }
  }

  @Override
  public boolean save(String id, String value) {
    try (var commands = getCommands()) {
      return "OK".equals(commands.set(createId(id), value));
    }
  }

  @Override
  public boolean saveWithExpiry(String id, String value, long expirySeconds) {
    try (var commands = getCommands()) {
      return "OK".equals(commands.setex(createId(id), expirySeconds, value));
    }
  }

  @Override
  public boolean saveMany(Map<String, String> entries) {
    try (var commands = getCommands()) {
      Map<String, String> map = new LinkedHashMap<>();
      for (Map.Entry<String, String> entry : entries.entrySet()) {
        map.put(createId(entry.getKey()), entry.getValue());
      }
      return "OK".equals(commands.mset(map));
    }
  }

  @Override
  public Optional<String> get(String id) {
    try (var commands = getCommands()) {
      String value = commands.get(createId(id));
      return Optional.ofNullable(value);
    }
  }

  @Override
  public Map<String, String> getMany(Collection<String> ids) {
    try (var commands = getCommands()) {
     // Convert keys to their prefixed versions
      String[] prefixedKeys = ids.stream().map(this::createId).toArray(String[]::new);

     // Fetch all values in a single operation
      List<KeyValue<String, String>> keyValues = commands.mget(prefixedKeys);

     // Create result map, excluding null values
      Map<String, String> result = new HashMap<>();
      for (KeyValue<String, String> keyValue : keyValues) {
        String key = keyValue.getKey();
        String value = keyValue.getValue();
        if (value != null) {
          result.put(removePrefix(key), value);
        }
      }
      return result;
    }
  }

  public Optional<String> getSet(String id, String value) {
    try (var commands = getCommands()) {
      return Optional.ofNullable(commands.getset(createId(id), value));
    }
  }

  public Optional<String> getDel(String id) {
    try (var commands = getCommands()) {
      return Optional.ofNullable(commands.getdel(createId(id)));
    }
  }

  public Optional<String> getEx(String id, long expirySeconds) {
    try (var commands = getCommands()) {
      return Optional.ofNullable(commands.getex(createId(id), GetExArgs.Builder.ex(expirySeconds)));
    }
  }

  public Optional<String> getPersist(String id) {
    try (var commands = getCommands()) {
      return Optional.ofNullable(commands.getex(createId(id), GetExArgs.Builder.persist()));
    }
  }

  @Override
  public long addToList(String id, String value) {
    try (var commands = getCommands()) {
      return commands.rpush(createId(id), value);
    }
  }

  @Override
  public long addAllToList(String id, List<String> values) {
    try (var commands = getCommands()) {
      return commands.rpush(createId(id), values.toArray(new String[0]));
    }
  }

  @Override
  public Optional<String> popFromList(String id) {
    try (var commands = getCommands()) {
      String value = commands.lpop(createId(id));
      return Optional.ofNullable(value);
    }
  }

  @Override
  public Optional<String> popFromListEnd(String id) {
    try (var commands = getCommands()) {
      String value = commands.rpop(createId(id));
      return Optional.ofNullable(value);
    }
  }

  @Override
  public List<String> getList(String id) {
    try (var commands = getCommands()) {
      List<String> values = commands.lrange(createId(id), 0, -1);
      return values != null ? values : Collections.emptyList();
    }
  }

  @Override
  public long addToSet(String id, String... values) {
    try (var commands = getCommands()) {
      return commands.sadd(createId(id), values);
    }
  }

  @Override
  public long removeFromSet(String id, String... values) {
    try (var commands = getCommands()) {
      return commands.srem(createId(id), values);
    }
  }

  @Override
  public Set<String> getSetMembers(String id) {
    try (var commands = getCommands()) {
      Set<String> members = commands.smembers(createId(id));
      return members != null ? members : Collections.emptySet();
    }
  }

  @Override
  public boolean isSetMember(String id, String value) {
    try (var commands = getCommands()) {
      return Boolean.TRUE.equals(commands.sismember(createId(id), value));
    }
  }

  @Override
  public long increment(String id) {
    try (var commands = getCommands()) {
      return commands.incr(createId(id));
    }
  }

  @Override
  public long incrementBy(String id, long amount) {
    try (var commands = getCommands()) {
      return commands.incrby(createId(id), amount);
    }
  }

  @Override
  public long decrement(String id) {
    try (var commands = getCommands()) {
      return commands.decr(createId(id));
    }
  }

  @Override
  public List<String> findKeysByPattern(String pattern) {
    try (var commands = getCommands()) {
      return commands.keys(createId(pattern)).stream()
          .map(this::removePrefix)
          .collect(Collectors.toList());
    }
  }

  @Override
  public boolean exists(String id) {
    try (var commands = getCommands()) {
      return commands.exists(createId(id)) > 0;
    }
  }

  @Override
  public Optional<Long> getTimeToLive(String id) {
    try (var commands = getCommands()) {
      Long ttl = commands.ttl(createId(id));
      return ttl != null && ttl >= 0 ? Optional.of(ttl) : Optional.empty();
    }
  }

  @Override
  public boolean updateExpiry(String id, long expirySeconds) {
    try (var commands = getCommands()) {
      return Boolean.TRUE.equals(commands.expire(createId(id), expirySeconds));
    }
  }

  @Override
  public boolean delete(String id) {
    try (var commands = getCommands()) {
      return commands.del(createId(id)) > 0;
    }
  }

  @Override
  public long deleteMany(Collection<String> ids) {
    try (var commands = getCommands()) {
      String[] keys = CollectionUtils.transformToArray(ids, String[]::new, this::createId);
      return commands.del(keys);
    }
  }

  @Override
  public String loadStandardScript(String scriptId, boolean force) {
    String script = StandardScripts.getScript(scriptId);
    if (force) {
      return redisClient.forceRegisterScript(scriptId, script);
    } else {
      return redisClient.registerScript(scriptId, script);
    }
  }

  @Override
  public String getKeyNamespace() {
    return prefix;
  }

  @Override
  public RedisCommands getRedisCommands() {
    return getCommands();
  }

  private RedisCommands getCommands() {
    return redisClient.getRedisCommands();
  }

  private String createId(String id) {
    return getKeyNamespace() + id;
  }

  private String removePrefix(String key) {
    return key.substring(getKeyNamespace().length());
  }
}
