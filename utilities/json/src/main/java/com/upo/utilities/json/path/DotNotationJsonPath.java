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

  private final List<ExtractorAndKey> sections;

  public DotNotationJsonPath(String path) {
    this.sections = createExtractors(path);
  }

  @Override
  public Object read(Object object) {
    Object current = object;
    try {
      for (var section : sections) {
        current = section.extractor.apply(current);
        if (current == null) {
          return null;
        }
      }
      return current;
    } catch (Exception e) {
      throw new RuntimeException("Error extracting path", e);
    }
  }

  @Override
  public String getToken(int index) {
    if (index < 0 || index >= sections.size()) {
      return null;
    }
    int i = 0;
    for (ExtractorAndKey section : sections) {
      if (section.key == null) {
        continue;
      }
      if (i == index) {
        return section.key;
      }
      i++;
    }
    return null;
  }

  private static List<ExtractorAndKey> createExtractors(String path) {
    List<ExtractorAndKey> extractors = new ArrayList<>();
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
        extractors.add(ExtractorAndKey.of(quotedKey, createMapExtractor(quotedKey)));
        if (quotedArrayIndex != null) {
          extractors.add(
              ExtractorAndKey.of(null, createArrayExtractor(Integer.parseInt(quotedArrayIndex))));
        }
      } else if (unquotedKey != null) {
        extractors.add(ExtractorAndKey.of(unquotedKey, createMapExtractor(unquotedKey)));
        if (unquotedArrayIndex != null) {
          extractors.add(
              ExtractorAndKey.of(null, createArrayExtractor(Integer.parseInt(unquotedArrayIndex))));
        }
      } else if (arrayIndex != null) {
        extractors.add(
            ExtractorAndKey.of(arrayIndex, createArrayExtractor(Integer.parseInt(arrayIndex))));
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

  private static final class ExtractorAndKey {
    private String key;
    Function<Object, Object> extractor;

    public ExtractorAndKey(String key, Function<Object, Object> extractor) {
      this.key = key;
      this.extractor = extractor;
    }

    public static ExtractorAndKey of(String key, Function<Object, Object> extractor) {
      return new ExtractorAndKey(key, extractor);
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public Function<Object, Object> getExtractor() {
      return extractor;
    }

    public void setExtractor(Function<Object, Object> extractor) {
      this.extractor = extractor;
    }
  }
}
