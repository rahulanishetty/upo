/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.upo.utilities.ds.CollectionUtils;

abstract class AbstractRedisClient implements RedisClient {

  private final Map<String, String> scriptIdVsScriptSha = new ConcurrentHashMap<>();

  @Override
  public String registerScript(String scriptId, String script) {
    return scriptIdVsScriptSha.computeIfAbsent(
        Objects.requireNonNull(scriptId, "scriptId cannot be null"),
        ignore -> {
          try (RedisCommands redisCommands = getRedisCommands()) {
            String digest =
                redisCommands.digest(Objects.requireNonNull(script, "script cannot be null"));
            List<Boolean> booleans = getRedisCommands().scriptExists(digest);
            if (CollectionUtils.isNotEmpty(booleans)) {
              if (Boolean.TRUE.equals(booleans.getFirst())) {
                return digest;
              }
            }
            String sha =
                Objects.requireNonNull(
                    redisCommands.scriptLoad(script), "loaded script sha shouldn't be null");
            if (!Objects.equals(sha, digest)) {
              throw new IllegalStateException("shouldn't happen");
            }
            return digest;
          }
        });
  }

  @Override
  public String forceRegisterScript(String scriptId, String script) {
    scriptIdVsScriptSha.remove(scriptId);
    return registerScript(scriptId, script);
  }
}
