/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.upo.resource.client.base.TestResourceConfigProvider;
import com.upo.resource.redis.impl.JsonRedisCodec;
import com.upo.resource.redis.impl.JsonRepositoryServiceImpl;
import com.upo.resource.redis.impl.RedisTemplateFactoryImpl;
import com.upo.utilities.context.RequestContextExtension;
import com.upo.utilities.context.WithRequestContext;

@Testcontainers
@ExtendWith(RequestContextExtension.class)
@WithRequestContext
class JsonRepositoryServiceTest {

  @SuppressWarnings("resource")
  @Container
  private static final GenericContainer<?> redis =
      new GenericContainer<>("redis/redis-stack-server:latest").withExposedPorts(6379);

  private RedisTemplateFactoryImpl redisTemplateFactory;
  private JsonRepositoryService<TestEntity, String> repositoryService;

  @BeforeEach
  void setUp() {
    String host = redis.getHost();
    Integer port = redis.getFirstMappedPort();

    TestResourceConfigProvider testResourceConfigProvider = new TestResourceConfigProvider();
    TestUtils.registerRedisServerConfig(host, port, testResourceConfigProvider);
    TestUtils.registerRedisTemplateResourceConfig("TEST_ENTITY", "1", testResourceConfigProvider);
    redisTemplateFactory = new RedisTemplateFactoryImpl(testResourceConfigProvider);
    repositoryService =
        new JsonRepositoryServiceImpl<>(
            redisTemplateFactory,
            () -> "TEST_ENTITY",
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
  void updateFieldIf_WhenFieldMatches_ShouldUpdate() {
   // Given
    TestEntity entity = new TestEntity("1", "test", 1L);
    repositoryService.save(entity);

    TestEntity updatedEntity = new TestEntity("1", "updated", 2L);

   // When
    Optional<TestEntity> result = repositoryService.updateIf(updatedEntity, "version", "1", true);

   // Then
    assertTrue(result.isPresent());
    assertEquals(entity, result.get());// Should return old value
  }
}
