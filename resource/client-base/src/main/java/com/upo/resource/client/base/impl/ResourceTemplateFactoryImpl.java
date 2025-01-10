/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.client.base.impl;

import java.io.Closeable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.upo.resource.client.base.*;
import com.upo.resource.client.base.models.ResourceCategory;
import com.upo.resource.client.base.models.ResourceConfig;
import com.upo.resource.client.base.models.ResourceType;
import com.upo.resource.client.base.models.TenantResourceConfig;

public abstract class ResourceTemplateFactoryImpl<
        Template,
        Client extends Closeable,
        TemplateConfig extends TenantResourceConfig,
        ServerConfig extends ResourceConfig>
    extends ResourceClientFactoryImpl<Client, ServerConfig>
    implements ResourceTemplateFactory<Template> {

  private final ResourceCategory resourceCategory;
  private final Map<String, Optional<Template>> templateCache;

  public ResourceTemplateFactoryImpl(
      ResourceConfigProvider resourceConfigProvider, ResourceCategory resourceCategory) {
    super(resourceConfigProvider);
    this.resourceCategory = resourceCategory;
    this.templateCache = new ConcurrentHashMap<>();
  }

  @Override
  public Template getTemplateOrFail(ResourceType resourceType, String tenantId) {
    return getTemplate(resourceType, tenantId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "failed to create template for category: "
                        + resourceCategory.name()
                        + ", type: "
                        + resourceType.name()
                        + ", tenantId: "
                        + tenantId));
  }

  @Override
  public Optional<Template> getTemplate(ResourceType resourceType, String tenantId) {
    return templateCache.computeIfAbsent(
        createTenantResourceId(resourceType, tenantId),
        tenantResourceId -> {
          TemplateConfig config = resourceConfigProvider.getConfig(tenantResourceId);
          if (config == null) {
            return Optional.empty();
          }
          String resourceIdSuffix = config.getResourceIdSuffix();
          return getClient(resourceCategory, resourceIdSuffix)
              .map(client -> createTemplate(client, config));
        });
  }

  protected abstract Template createTemplate(Client client, TemplateConfig config);

  private String createTenantResourceId(ResourceType resourceType, String tenantId) {
    return resourceCategory.name() + "/" + resourceType.name() + "/" + tenantId;
  }
}
