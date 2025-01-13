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

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.upo.resource.client.base.TestResourceConfigProvider;
import com.upo.resource.redis.impl.JsonRedisCodec;
import com.upo.resource.redis.impl.RedisTemplateFactoryImpl;
import com.upo.resource.redis.impl.RepositoryServiceImpl;
import com.upo.utilities.context.RequestContextExtension;
import com.upo.utilities.context.WithRequestContext;

@Testcontainers
@ExtendWith(RequestContextExtension.class)
@WithRequestContext
class RepositoryServiceTest {

  @SuppressWarnings("resource")
  @Container
  private static final GenericContainer<?> redis =
      new GenericContainer<>("redis:latest").withExposedPorts(6379);

  private RedisTemplateFactoryImpl redisTemplateFactory;
  private RepositoryService<TestEntity, String> repositoryService;

  @BeforeEach
  void setUp() {
    String host = redis.getHost();
    Integer port = redis.getFirstMappedPort();

    String testId = UUID.randomUUID().toString();
    TestResourceConfigProvider testResourceConfigProvider = new TestResourceConfigProvider();
    TestUtils.registerRedisServerConfig(host, port, testResourceConfigProvider);
    TestUtils.registerRedisTemplateResourceConfig(
        "TEST_ENTITY_" + testId, "1", testResourceConfigProvider);
    redisTemplateFactory = new RedisTemplateFactoryImpl(testResourceConfigProvider);
    repositoryService =
        new RepositoryServiceImpl<>(
            redisTemplateFactory,
            () -> "TEST_ENTITY_" + testId,
            TestEntity.class,
            JsonRedisCodec.forStringKey(TestEntity.class, TestEntity::getId));
  }

  @AfterEach
  public void cleanup() throws IOException {
    repositoryService = null;
    redisTemplateFactory.close();
    redisTemplateFactory = null;
  }

  @Test
  void insert_WhenEntityDoesNotExist_ShouldInsert() {
   // Given
    TestEntity entity = new TestEntity("1", "test");

   // When
    boolean result = repositoryService.insert(entity);

   // Then
    assertTrue(result);
    Optional<TestEntity> saved = repositoryService.findById("1");
    assertTrue(saved.isPresent());
    assertEquals(entity, saved.get());
  }

  @Test
  void saveWithExpiry_ShouldSaveAndExpire() throws InterruptedException {
   // Given
    TestEntity entity = new TestEntity("1", "test");

   // When
    boolean result = repositoryService.saveWithExpiry(entity, 1);

   // Then
    assertTrue(result);
    assertTrue(repositoryService.exists("1"));

   // Wait for expiry
    Thread.sleep(1500);
    assertFalse(repositoryService.exists("1"));
  }

  @Test
  @DisplayName("insertMany should insert all objects when none exist")
  void insertMany_WhenNoObjectsExist_ShouldInsertAll() {
   // Given
    List<TestEntity> entities =
        Arrays.asList(new TestEntity("1", "test1"), new TestEntity("2", "test2"));

   // When
    boolean result = repositoryService.insertMany(entities);

   // Then
    assertTrue(result);
    Map<String, TestEntity> saved = repositoryService.findByIds(Arrays.asList("1", "2"));
    assertEquals(2, saved.size());
    assertEquals("test1", saved.get("1").getName());
    assertEquals("test2", saved.get("2").getName());
  }

  @Test
  @DisplayName("insertMany should fail if any object exists")
  void insertMany_WhenOneObjectExists_ShouldNotInsertAny() {
   // Given
    TestEntity existing = new TestEntity("1", "existing");
    repositoryService.save(existing);

    List<TestEntity> entities =
        Arrays.asList(new TestEntity("1", "test1"), new TestEntity("2", "test2"));

   // When
    boolean result = repositoryService.insertMany(entities);

   // Then
    assertFalse(result);
    Map<String, TestEntity> saved = repositoryService.findByIds(Arrays.asList("1", "2"));
    assertEquals(1, saved.size());
    assertEquals("existing", saved.get("1").getName());
  }

  @Test
  @DisplayName("saveMany should save all objects")
  void saveMany_ShouldSaveAllObjects() {
   // Given
    List<TestEntity> entities =
        Arrays.asList(new TestEntity("1", "test1"), new TestEntity("2", "test2"));

   // When
    boolean result = repositoryService.saveMany(entities);

   // Then
    assertTrue(result);
    Map<String, TestEntity> saved = repositoryService.findByIds(Arrays.asList("1", "2"));
    assertEquals(2, saved.size());
    assertEquals("test1", saved.get("1").getName());
    assertEquals("test2", saved.get("2").getName());
  }

  @Test
  @DisplayName("findByIds should return only existing objects")
  void findByIds_ShouldReturnOnlyExistingObjects() {
   // Given
    TestEntity entity = new TestEntity("1", "test1");
    repositoryService.save(entity);

   // When
    Map<String, TestEntity> result = repositoryService.findByIds(Arrays.asList("1", "2"));

   // Then
    assertEquals(1, result.size());
    assertEquals("test1", result.get("1").getName());
    assertNull(result.get("2"));
  }

  @Test
  @DisplayName("deleteById should delete existing object")
  void deleteById_WhenObjectExists_ShouldDelete() {
   // Given
    TestEntity entity = new TestEntity("1", "test1");
    repositoryService.save(entity);

   // When
    boolean result = repositoryService.deleteById("1");

   // Then
    assertTrue(result);
    assertTrue(repositoryService.findById("1").isEmpty());
  }

  @Test
  @DisplayName("deleteByIds should delete multiple objects")
  void deleteByIds_ShouldDeleteMultipleObjects() {
   // Given
    List<TestEntity> entities =
        Arrays.asList(new TestEntity("1", "test1"), new TestEntity("2", "test2"));
    repositoryService.saveMany(entities);

   // When
    long deleted = repositoryService.deleteByIds(Arrays.asList("1", "2", "3"));

   // Then
    assertEquals(2, deleted);
    assertTrue(repositoryService.findById("1").isEmpty());
    assertTrue(repositoryService.findById("2").isEmpty());
  }

  @Test
  @DisplayName("getAndDelete should return and delete object")
  void getAndDelete_WhenObjectExists_ShouldReturnAndDelete() {
   // Given
    TestEntity entity = new TestEntity("1", "test1");
    repositoryService.save(entity);

   // When
    Optional<TestEntity> result = repositoryService.getAndDelete("1");

   // Then
    assertTrue(result.isPresent());
    assertEquals("test1", result.get().getName());
    assertTrue(repositoryService.findById("1").isEmpty());
  }

  @Test
  @DisplayName("findByPattern should return matching objects")
  void findByPattern_ShouldReturnMatchingObjects() {
   // Given
    repositoryService.save(new TestEntity("user:1", "test1"));
    repositoryService.save(new TestEntity("user:2", "test2"));
    repositoryService.save(new TestEntity("other:3", "test3"));

   // When
    List<TestEntity> results = repositoryService.findByPattern("user:*");

   // Then
    assertEquals(2, results.size());
    assertTrue(results.stream().anyMatch(e -> e.getName().equals("test1")));
    assertTrue(results.stream().anyMatch(e -> e.getName().equals("test2")));
  }

  @Test
  @DisplayName("getTimeToLive should return TTL for object with expiry")
  void getTimeToLive_WhenObjectHasExpiry_ShouldReturnTTL() {
   // Given
    TestEntity entity = new TestEntity("1", "test1");
    repositoryService.saveWithExpiry(entity, 60);

   // When
    Optional<Long> ttl = repositoryService.getTimeToLive("1");

   // Then
    assertTrue(ttl.isPresent());
    assertTrue(ttl.get() <= 60 && ttl.get() > 0);
  }

  @Test
  @DisplayName("updateExpiry should update object TTL")
  void updateExpiry_WhenObjectExists_ShouldUpdateTTL() {
   // Given
    TestEntity entity = new TestEntity("1", "test1");
    repositoryService.save(entity);

   // When
    boolean result = repositoryService.updateExpiry("1", 30);

   // Then
    assertTrue(result);
    Optional<Long> ttl = repositoryService.getTimeToLive("1");
    assertTrue(ttl.isPresent());
    assertTrue(ttl.get() <= 30 && ttl.get() > 0);
  }

  @Nested
  @DisplayName("Edge Cases")
  @WithRequestContext
  class EdgeCases {

    @Test
    @DisplayName("insertMany should handle empty collection")
    void insertMany_WithEmptyCollection_ShouldReturnTrue() {
      assertTrue(repositoryService.insertMany(Collections.emptyList()));
    }

    @Test
    @DisplayName("saveMany should handle empty collection")
    void saveMany_WithEmptyCollection_ShouldReturnTrue() {
      assertTrue(repositoryService.saveMany(Collections.emptyList()));
    }

    @Test
    @DisplayName("findByIds should handle empty collection")
    void findByIds_WithEmptyCollection_ShouldReturnEmptyMap() {
      assertTrue(repositoryService.findByIds(Collections.emptyList()).isEmpty());
    }

    @Test
    @DisplayName("deleteByIds should handle empty collection")
    void deleteByIds_WithEmptyCollection_ShouldReturnZero() {
      assertEquals(0, repositoryService.deleteByIds(Collections.emptyList()));
    }

    @Test
    @DisplayName("getTimeToLive should handle non-existent object")
    void getTimeToLive_WhenObjectDoesNotExist_ShouldReturnEmpty() {
      assertTrue(repositoryService.getTimeToLive("nonexistent").isEmpty());
    }
  }
}
