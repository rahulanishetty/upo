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
                  Script performs atomic update using cjson
                  KEYS[1] = key to update
                  ARGV[1] = new value (JSON string)
                  ARGV[2] = json path (e.g., "metadata.version" or "roles[0].name")
                  ARGV[3] = expected value for the field
                  ARGV[4] = return old value if "true", new value if "false"
                 \s
                  Returns:
                  {returnedValue, 1} - if update successful
                  {nil, 0} - if condition not met or key doesn't exist
              --]]

              -- Helper function to get value from path
              local function get_value_by_path(json, path)
                  local current = json
                 \s
                  -- Split path by dots
                  for field in string.gmatch(path, "[^%.]+") do
                      if current == nil then
                          return nil
                      end
                     \s
                      -- Check for array access [n]
                      local array_index = string.match(field, "^(%d+)$")
                      if array_index then
                          -- Arrays in Lua are 1-based
                          current = current[tonumber(array_index) + 1]
                      else
                          current = current[field]
                      end
                  end
                 \s
                  return current
              end

              -- Get current value
              local currentValue = redis.call('GET', KEYS[1])
              if not currentValue then
                  return {nil, 0}
              end

              -- Parse JSON and get field value
              local success, json = pcall(cjson.decode, currentValue)
              if not success then
                  return {nil, 0}
              end

              local fieldValue = get_value_by_path(json, ARGV[2])
              if fieldValue == nil then
                  return {nil, 0}
              end

              -- Compare value (convert both to string for comparison)
              if tostring(fieldValue) ~= ARGV[3] then
                  return {nil, 0}
              end

              -- Use SET for update
              redis.call('SET', KEYS[1], ARGV[1])

              -- Return based on flag
              local returnOld = ARGV[4] == "true"
              return {returnOld and currentValue or ARGV[1], 1}
              """);

  static String getScript(String scriptId) {
    return SCRIPT.get(scriptId);
  }
}
