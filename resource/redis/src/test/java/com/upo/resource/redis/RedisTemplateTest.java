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
import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.upo.resource.client.base.TestResourceConfigProvider;
import com.upo.resource.redis.impl.RedisTemplateFactoryImpl;

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
    String testId = UUID.randomUUID().toString();
    TestUtils.registerRedisTemplateResourceConfig(
        "TEST_ENTITY_" + testId, "1", testResourceConfigProvider);
    redisTemplateFactory = new RedisTemplateFactoryImpl(testResourceConfigProvider);
    redisTemplate = redisTemplateFactory.getRedisTemplate(() -> "TEST_ENTITY_" + testId, "1");
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

  @Test
  void insertWithExpiry_WhenKeyDoesNotExist_ShouldInsertWithTTL() throws InterruptedException {
   // Given
    String key = "test-key";
    String value = "test-value";

   // When
    boolean result = redisTemplate.insertWithExpiry(key, value, 1);

   // Then
    assertTrue(result);
    assertEquals(Optional.of(value), redisTemplate.get(key));
    assertTrue(redisTemplate.getTimeToLive(key).orElse(0L) <= 1);

   // Verify expiry
    Thread.sleep(1500);
    assertFalse(redisTemplate.exists(key));
  }

  @Test
  void saveWithExpiry_ShouldOverwriteAndSetTTL() {
   // Given
    String key = "test-key";
    String originalValue = "original-value";
    String newValue = "new-value";
    redisTemplate.save(key, originalValue);

   // When
    boolean result = redisTemplate.saveWithExpiry(key, newValue, 60);

   // Then
    assertTrue(result);
    assertEquals(Optional.of(newValue), redisTemplate.get(key));
    assertTrue(redisTemplate.getTimeToLive(key).orElse(0L) <= 60);
  }

  @Test
  void saveMany_ShouldOverwriteExistingKeys() {
   // Given
    String key1 = "key1";
    String key2 = "key2";
    redisTemplate.save(key1, "old-value1");
    Map<String, String> entries = new HashMap<>();
    entries.put(key1, "new-value1");
    entries.put(key2, "value2");

   // When
    boolean result = redisTemplate.saveMany(entries);

   // Then
    assertTrue(result);
    assertEquals(Optional.of("new-value1"), redisTemplate.get(key1));
    assertEquals(Optional.of("value2"), redisTemplate.get(key2));
  }

  @Test
  void getEx_ShouldReturnValueAndSetExpiry() {
   // Given
    String key = "test-key";
    String value = "test-value";
    redisTemplate.save(key, value);

   // When
    Optional<String> result = redisTemplate.getEx(key, 30);

   // Then
    assertTrue(result.isPresent());
    assertEquals(value, result.get());
    assertTrue(redisTemplate.getTimeToLive(key).orElse(0L) <= 30);
  }

  @Test
  void getPersist_ShouldRemoveExpiry() {
   // Given
    String key = "test-key";
    String value = "test-value";
    redisTemplate.saveWithExpiry(key, value, 60);

   // When
    Optional<String> result = redisTemplate.getPersist(key);

   // Then
    assertTrue(result.isPresent());
    assertEquals(value, result.get());
    assertTrue(redisTemplate.getTimeToLive(key).isEmpty());
  }

  @Test
  void updateExpiry_ShouldUpdateTTL() {
   // Given
    String key = "test-key";
    redisTemplate.saveWithExpiry(key, "value", 60);

   // When
    boolean result = redisTemplate.updateExpiry(key, 30);

   // Then
    assertTrue(result);
    assertTrue(redisTemplate.getTimeToLive(key).orElse(0L) <= 30);
  }

  @Test
  void deleteMany_ShouldRemoveMultipleKeys() {
   // Given
    redisTemplate.save("key1", "value1");
    redisTemplate.save("key2", "value2");
    redisTemplate.save("key3", "value3");

   // When
    long deleted = redisTemplate.deleteMany(Arrays.asList("key1", "key2", "nonexistent"));

   // Then
    assertEquals(2, deleted);
    assertFalse(redisTemplate.exists("key1"));
    assertFalse(redisTemplate.exists("key2"));
    assertTrue(redisTemplate.exists("key3"));
  }

  @Test
  void getMany_ShouldReturnExistingValues() {
   // Given
    redisTemplate.save("key1", "value1");
    redisTemplate.save("key2", "value2");

   // When
    Map<String, String> results = redisTemplate.getMany(Arrays.asList("key1", "key2", "key3"));

   // Then
    assertEquals(2, results.size());
    assertEquals("value1", results.get("key1"));
    assertEquals("value2", results.get("key2"));
    assertNull(results.get("key3"));
  }

  @Test
  void addToList_ShouldAppendToList() {
   // Given
    String key = "list-key";

   // When
    long length = redisTemplate.addToList(key, "value1");
    redisTemplate.addToList(key, "value2");

   // Then
    assertEquals(1, length);
    List<String> list = redisTemplate.getList(key);
    assertEquals(2, list.size());
    assertEquals("value1", list.get(0));
    assertEquals("value2", list.get(1));
  }

  @Test
  void addAllToList_ShouldAppendMultipleValues() {
   // Given
    String key = "list-key";
    List<String> values = Arrays.asList("value1", "value2", "value3");

   // When
    long length = redisTemplate.addAllToList(key, values);

   // Then
    assertEquals(3, length);
    assertEquals(values, redisTemplate.getList(key));
  }

  @Test
  void popOperations_ShouldWorkAsExpected() {
   // Given
    String key = "list-key";
    redisTemplate.addAllToList(key, Arrays.asList("first", "middle", "last"));

   // When/Then
    Optional<String> first = redisTemplate.popFromList(key);
    assertTrue(first.isPresent());
    assertEquals("first", first.get());

    Optional<String> last = redisTemplate.popFromListEnd(key);
    assertTrue(last.isPresent());
    assertEquals("last", last.get());

    List<String> remaining = redisTemplate.getList(key);
    assertEquals(1, remaining.size());
    assertEquals("middle", remaining.get(0));
  }

  @Test
  void setOperations_ShouldWorkAsExpected() {
   // Given
    String key = "set-key";

   // When
    long added = redisTemplate.addToSet(key, "value1", "value2", "value1");

   // Then
    assertEquals(2, added);
    Set<String> members = redisTemplate.getSetMembers(key);
    assertEquals(2, members.size());
    assertTrue(members.contains("value1"));
    assertTrue(members.contains("value2"));

    assertTrue(redisTemplate.isSetMember(key, "value1"));
    assertFalse(redisTemplate.isSetMember(key, "nonexistent"));

    long removed = redisTemplate.removeFromSet(key, "value1", "nonexistent");
    assertEquals(1, removed);
    assertEquals(1, redisTemplate.getSetMembers(key).size());
  }

  @Test
  void counterOperations_ShouldWorkAsExpected() {
   // Given
    String key = "counter-key";

   // When/Then
    assertEquals(1, redisTemplate.increment(key));
    assertEquals(3, redisTemplate.incrementBy(key, 2));
    assertEquals(2, redisTemplate.decrement(key));
  }
}
