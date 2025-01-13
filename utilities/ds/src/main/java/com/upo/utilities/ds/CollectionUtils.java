/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.ds;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;

public class CollectionUtils {

  public static boolean isEmpty(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  public static boolean isEmpty(Map<?, ?> map) {
    return map == null || map.isEmpty();
  }

  public static <T> boolean isNotEmpty(Collection<T> collection) {
    return !isEmpty(collection);
  }

  public static <T1, T2> List<T2> transformToList(
      Collection<T1> collection, Function<T1, T2> function) {
    if (isEmpty(collection)) {
      return Collections.emptyList();
    }
    List<T2> result = new ArrayList<>();
    for (T1 t1 : collection) {
      T2 t2 = function.apply(t1);
      if (t2 != null) {
        result.add(t2);
      }
    }
    return result;
  }

  public static <V1, V2> V2[] transformToArray(
      Collection<V1> collection, IntFunction<V2[]> creator, Function<V1, V2> function) {
    if (isEmpty(collection)) {
      return creator.apply(0);
    }
    V2[] result = creator.apply(collection.size());
    int index = 0;
    for (V1 v1 : collection) {
      result[index++] = function.apply(v1);
    }
    return result;
  }

  public static <K1, K2, V> Map<K2, V> transformKeyInMap(
      Map<K1, V> map, Function<K1, K2> function) {
    return transformMap(map, function, Function.identity());
  }

  public static <K, V1, V2> Map<K, V2> transformValueInMap(
      Map<K, V1> map, Function<V1, V2> function) {
    return transformMap(map, Function.identity(), function);
  }

  public static <K1, V1, K2, V2> Map<K2, V2> transformMap(
      Map<K1, V1> map, Function<K1, K2> keyFunction, Function<V1, V2> valueFunction) {
    if (isEmpty(map)) {
      return Collections.emptyMap();
    }
    Map<K2, V2> result = new LinkedHashMap<>();
    for (Map.Entry<K1, V1> entry : map.entrySet()) {
      K2 k2 = keyFunction.apply(entry.getKey());
      if (k2 == null) {
        continue;
      }
      V2 v2 = valueFunction.apply(entry.getValue());
      if (v2 == null) {
        continue;
      }
      result.put(k2, v2);
    }
    return result;
  }

  public static <T, K> Map<K, T> transfromToMap(
      Collection<T> collection, Function<T, K> idProvider) {
    return transformToMap(collection, idProvider, Function.identity());
  }

  public static <T, K, V> Map<K, V> transformToMap(
      Collection<T> collection, Function<T, K> idProvider, Function<T, V> valueProvider) {
    if (isEmpty(collection)) {
      return Collections.emptyMap();
    }
    Map<K, V> result = new LinkedHashMap<>();
    for (T obj : collection) {
      K key = idProvider.apply(obj);
      if (key == null) {
        continue;
      }
      V value = valueProvider.apply(obj);
      if (value == null) {
        continue;
      }
      result.put(key, value);
    }
    return result;
  }
}
