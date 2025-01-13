/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.upo.resource.client.base.TestResourceConfigProvider;

@Testcontainers
public class RedisTemplateTest {

  @SuppressWarnings("resource")
  @Container
  private static final GenericContainer<?> redis =
      new GenericContainer<>("redis:latest").withExposedPorts(6379);

  private RedisTemplateFactoryImpl redisTemplateFactory;
  private RedisTemplate redisTemplate;

  @BeforeEach
  void setUp() {
    String host = redis.getHost();
    Integer port = redis.getFirstMappedPort();
    TestResourceConfigProvider testResourceConfigProvider = new TestResourceConfigProvider();
    TestUtils.registerRedisServerConfig(host, port, testResourceConfigProvider);
    TestUtils.registerRedisTemplateResourceConfig("TEST_ENTITY", "1", testResourceConfigProvider);
    redisTemplateFactory = new RedisTemplateFactoryImpl(testResourceConfigProvider);
    redisTemplate = redisTemplateFactory.getRedisTemplate(() -> "TEST_ENTITY", "1");
  }

  @AfterEach
  public void cleanup() throws IOException {
    redisTemplate = null;
    redisTemplateFactory.close();
    redisTemplateFactory = null;
  }

  @Test
  void insert_WhenKeyDoesNotExist_ShouldInsertValue() {
   // Given
    String key = "test-key";
    String value = "test-value";

   // When
    boolean result = redisTemplate.insert(key, value);

   // Then
    assertTrue(result);
    assertEquals(Optional.of(value), redisTemplate.get(key));
  }

  @Test
  void insert_WhenKeyExists_ShouldNotOverwrite() {
   // Given
    String key = "test-key";
    String originalValue = "original-value";
    String newValue = "new-value";
    redisTemplate.insert(key, originalValue);

   // When
    boolean result = redisTemplate.insert(key, newValue);

   // Then
    assertFalse(result);
    assertEquals(Optional.of(originalValue), redisTemplate.get(key));
  }

  @Test
  void insertMany_WhenNoKeysExist_ShouldInsertAll() {
   // Given
    Map<String, String> entries = new HashMap<>();
    entries.put("key1", "value1");
    entries.put("key2", "value2");

   // When
    boolean result = redisTemplate.insertMany(entries);

   // Then
    assertTrue(result);
    assertEquals(Optional.of("value1"), redisTemplate.get("key1"));
    assertEquals(Optional.of("value2"), redisTemplate.get("key2"));
  }

  @Test
  void getSet_ShouldReturnOldValueAndSetNew() {
   // Given
    String key = "test-key";
    String oldValue = "old-value";
    String newValue = "new-value";
    redisTemplate.save(key, oldValue);

   // When
    Optional<String> result = redisTemplate.getSet(key, newValue);

   // Then
    assertTrue(result.isPresent());
    assertEquals(oldValue, result.get());
    assertEquals(Optional.of(newValue), redisTemplate.get(key));
  }

  @Test
  void getDel_ShouldReturnValueAndDelete() {
   // Given
    String key = "test-key";
    String value = "test-value";
    redisTemplate.save(key, value);

   // When
    Optional<String> result = redisTemplate.getDel(key);

   // Then
    assertTrue(result.isPresent());
    assertEquals(value, result.get());
    assertFalse(redisTemplate.exists(key));
  }
}
