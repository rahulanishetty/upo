/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.json.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DotNotationJsonPath implements JsonPath {

  private static final Pattern TOKEN_PATTERN =
      Pattern.compile(
          "\"([^\"]+)\"(?:\\[([0-9]+)])?|"// Quoted key with optional array index
              + "([^.\\[\\]\"]+)(?:\\[([0-9]+)])?|"// Unquoted key with optional array index
              + "\\[([0-9]+)]"// Standalone array index
          );

  private final List<Function<Object, Object>> extractors;

  public DotNotationJsonPath(String path) {
    this.extractors = createExtractors(path);
  }

  @Override
  public Object read(Object object) {
    Object current = object;
    try {
      for (Function<Object, Object> extractor : extractors) {
        current = extractor.apply(current);
        if (current == null) {
          return null;
        }
      }
      return current;
    } catch (Exception e) {
      throw new RuntimeException("Error extracting path", e);
    }
  }

  private static List<Function<Object, Object>> createExtractors(String path) {
    List<Function<Object, Object>> extractors = new ArrayList<>();
    Matcher matcher = TOKEN_PATTERN.matcher(path);
    int lastEnd = 0;

    while (matcher.find()) {
      String dots = path.substring(lastEnd, matcher.start());
      if (!dots.isEmpty() && !dots.equals(".")) {
        throw new IllegalArgumentException("Invalid path syntax near: " + dots);
      }
      lastEnd = matcher.end();

      String quotedKey = matcher.group(1);
      String quotedArrayIndex = matcher.group(2);
      String unquotedKey = matcher.group(3);
      String unquotedArrayIndex = matcher.group(4);
      String arrayIndex = matcher.group(5);

      if (quotedKey != null) {
        extractors.add(createMapExtractor(quotedKey));
        if (quotedArrayIndex != null) {
          extractors.add(createArrayExtractor(Integer.parseInt(quotedArrayIndex)));
        }
      } else if (unquotedKey != null) {
        extractors.add(createMapExtractor(unquotedKey));
        if (unquotedArrayIndex != null) {
          extractors.add(createArrayExtractor(Integer.parseInt(unquotedArrayIndex)));
        }
      } else if (arrayIndex != null) {
        extractors.add(createArrayExtractor(Integer.parseInt(arrayIndex)));
      }
    }

    if (lastEnd != path.length()) {
      throw new IllegalArgumentException("Invalid path syntax at end: " + path.substring(lastEnd));
    }
    return extractors;
  }

  private static Function<Object, Object> createArrayExtractor(int index) {
    return obj -> {
      if (obj instanceof List<?> list) {
        return index < list.size() ? list.get(index) : null;
      }
      if (obj instanceof Object[] array) {
        return index < array.length ? array[index] : null;
      }
      throw new IllegalArgumentException("Cannot apply array index to: " + obj.getClass());
    };
  }

  private static Function<Object, Object> createMapExtractor(String key) {
    return obj -> readValueForKey(key, obj);
  }

  private static Object readValueForKey(String key, Object obj) {
    if (obj instanceof Map) {
      return ((Map<?, ?>) obj).get(key);
    } else if (obj instanceof List<?> list) {
      List<Object> result = new ArrayList<>();
      for (Object item : list) {
        Object o = readValueForKey(key, item);
        if (o != null) {
          result.add(o);
        }
      }
      if (result.isEmpty()) {
        return null;
      }
      return result;
    } else if (obj instanceof Object[] array) {
      List<Object> result = new ArrayList<>();
      for (Object item : array) {
        Object o = readValueForKey(key, item);
        if (o != null) {
          result.add(o);
        }
      }
      if (result.isEmpty()) {
        return null;
      }
      return result;
    }
    throw new IllegalArgumentException("Cannot apply key to: " + obj.getClass());
  }
}
