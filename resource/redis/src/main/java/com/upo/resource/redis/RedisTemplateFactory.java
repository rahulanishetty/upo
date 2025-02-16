/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import com.upo.resource.client.base.models.ResourceType;

public interface RedisTemplateFactory {
  RedisTemplate getRedisTemplate(ResourceType resourceType, String partitionKey);
}
