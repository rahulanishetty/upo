/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import java.util.Map;

public final class StandardScripts {
  public static final String UPDATE_IF = "update-if";

  private static final Map<String, String> SCRIPT =
      Map.of(
          UPDATE_IF,
          """
              --[[
                  Script performs atomic update using RedisJSON
                  KEYS[1] = key to update
                  ARGV[1] = new value (JSON string)
                  ARGV[2] = JSON path (e.g., "$.metadata.version")
                  ARGV[3] = expected value for the field
                  ARGV[4] = return old value if "true", new value if "false"
                 \s
                  Returns:
                  {returnedValue, 1} - if update successful
                  {nil, 0} - if condition not met or key doesn't exist or path invalid
              --]]

              -- Get value at path using JSON.GET
              local fieldValue = redis.call('JSON.GET', KEYS[1], ARGV[2])
              if not fieldValue then
                  return {nil, 0}
              end

              -- Compare value (strip quotes if string)
              fieldValue = string.gsub(fieldValue, '^"(.*)"$', '%1')
              if fieldValue ~= ARGV[3] then
                  return {nil, 0}
              end

              -- Perform update using GETSET to return old value
              local oldValue = redis.call('GETSET', KEYS[1], ARGV[1])

              -- Return based on flag
              local returnOld = ARGV[4] == "true"
              return {returnOld and oldValue or ARGV[1], 1}
              """);

  static String getScript(String scriptId) {
    return SCRIPT.get(scriptId);
  }
}
