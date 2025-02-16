/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.json;

import java.lang.reflect.Type;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

public class Utils {

  public static final Type GENERIC_JSON_MAP_TYPE =
      new TypeReference<Map<String, Object>>() {}.getType();

  /** Creates a deep copy of an object by serializing to JSON and back. */
  public static <T> T deepCopyViaJson(Object obj, Type type) {
    if (obj == null) {
      return null;
    }
    return fromJson(toJson(obj), type);
  }

  public static Object toJsonObject(Object object) {
    if (object == null) {
      return null;
    }
    return JSON.toJSON(object);
  }

  public static String toJson(Object object) {
    if (object == null) {
      return null;
    }
    return JSON.toJSONString(object);
  }

  public static <T> T fromJson(String json, Type type) {
    if (json == null) {
      return null;
    }
    return JSON.parseObject(json, type);
  }

  public static <T> T fromJson(String json, Class<T> clz) {
    if (json == null) {
      return null;
    }
    return JSON.parseObject(json, clz);
  }
}
