/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.upo.orchestrator.engine.ProcessServices;

public class ProcessServicesImpl implements ProcessServices {

  private final Map<Class<?>, Object> services;
  private final ProcessServices coreRuntimeServices;

  public ProcessServicesImpl() {
    services = new ConcurrentHashMap<>();
    coreRuntimeServices = null;
  }

  public ProcessServicesImpl(ProcessServices coreRuntimeServices) {
    services = new ConcurrentHashMap<>();
    this.coreRuntimeServices = coreRuntimeServices;
  }

  public <T> void registerService(Class<T> clz, T service) {
    Object existing = services.putIfAbsent(clz, service);
    if (existing != null) {
      throw new IllegalArgumentException(
          "trying to register duplicate service for clz: "
              + clz
              + ", existing: "
              + services.get(clz)
              + ", missing: "
              + service);
    }
  }

  @Override
  public <T> T getService(Class<T> clz) {
   //noinspection unchecked
    T service = (T) services.get(clz);
    if (service != null) {
      return service;
    }
    return coreRuntimeServices != null ? coreRuntimeServices.getService(clz) : null;
  }
}
