/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import java.util.*;

import com.upo.resource.client.base.models.ResourceType;
import com.upo.utilities.context.RequestContext;
import com.upo.utilities.ds.CollectionUtils;

public class RepositoryServiceImpl<T, ID> implements RepositoryService<T, ID> {

  private final RedisTemplateFactory redisTemplateFactory;
  private final ResourceType resourceType;
  private final RedisCodec<T, ID> codec;
  private final String prefix;

  public RepositoryServiceImpl(
      RedisTemplateFactory redisTemplateFactory,
      ResourceType resourceType,
      Class<T> clz,
      RedisCodec<T, ID> codec) {
    this.redisTemplateFactory = redisTemplateFactory;
    this.resourceType = resourceType;
    this.codec = codec;
    this.prefix = clz.getSimpleName() + "/";
  }

  @Override
  public boolean insert(T obj) {
    return getRedisTemplate().insert(createKey(obj), toString(obj));
  }

  @Override
  public boolean insertMany(Collection<T> objects) {
    if (CollectionUtils.isEmpty(objects)) {
      return true;
    }
    Map<String, String> toInsert =
        CollectionUtils.transformToMap(objects, this::createKey, this::toString);
    return getRedisTemplate().insertMany(toInsert);
  }

  @Override
  public boolean save(T obj) {
    return getRedisTemplate().save(createKey(obj), toString(obj));
  }

  @Override
  public boolean saveMany(Collection<T> objects) {
    if (CollectionUtils.isEmpty(objects)) {
      return true;
    }
    Map<String, String> toSave =
        CollectionUtils.transformToMap(objects, this::createKey, this::toString);
    return getRedisTemplate().saveMany(toSave);
  }

  @Override
  public Optional<T> findById(ID id) {
    return getRedisTemplate().get(createKey(id.toString())).map(this::fromString);
  }

  @Override
  public Map<ID, T> findByIds(Collection<ID> ids) {
    if (CollectionUtils.isEmpty(ids)) {
      return Collections.emptyMap();
    }
    Map<String, String> out =
        getRedisTemplate().getMany(CollectionUtils.transformToList(ids, this::toKey));
    return CollectionUtils.transformMap(out, this::fromKey, this::fromString);
  }

  @Override
  public boolean deleteById(ID id) {
    return getRedisTemplate().delete(toKey(id));
  }

  @Override
  public long deleteByIds(Collection<ID> ids) {
    if (CollectionUtils.isEmpty(ids)) {
      return 0;
    }
    return getRedisTemplate().deleteMany(CollectionUtils.transformToList(ids, this::toKey));
  }

  @Override
  public Optional<T> getAndDelete(ID id) {
    return getRedisTemplate().getDel(toKey(id)).map(this::fromString);
  }

  @Override
  public boolean saveWithExpiry(T obj, long expirySeconds) {
    return getRedisTemplate().saveWithExpiry(createKey(obj), toString(obj), expirySeconds);
  }

  @Override
  public List<T> findByPattern(String pattern) {
    List<String> keys = getRedisTemplate().findKeysByPattern(createKey(pattern));
    if (keys.isEmpty()) {
      return Collections.emptyList();
    }
    Map<String, String> values = getRedisTemplate().getMany(keys);
    return CollectionUtils.transformToList(values.values(), this::fromString);
  }

  @Override
  public boolean exists(ID id) {
    return getRedisTemplate().exists(toKey(id));
  }

  @Override
  public Optional<Long> getTimeToLive(ID id) {
    return getRedisTemplate().getTimeToLive(toKey(id));
  }

  @Override
  public boolean updateExpiry(ID id, long expirySeconds) {
    return getRedisTemplate().updateExpiry(toKey(id), expirySeconds);
  }

  protected String createKey(T obj) {
    return createKey(codec.getId(obj));
  }

  private String createKey(String id) {
    return prefix + id;
  }

  private String toKey(ID id) {
    return createKey(codec.serializeId(id));
  }

  private ID fromKey(String key) {
    return codec.deserializeId(key.substring(prefix.length()));
  }

  protected String toString(T obj) {
    return codec.toString(obj);
  }

  protected T fromString(String value) {
    return codec.fromString(value);
  }

  RedisTemplate getRedisTemplate() {
    RequestContext requestContext = RequestContext.get();
    if (requestContext == null) {
      throw new IllegalStateException("RequestContext is not set!");
    }
    return redisTemplateFactory.getRedisTemplate(resourceType, requestContext.getPartitionKey());
  }
}
