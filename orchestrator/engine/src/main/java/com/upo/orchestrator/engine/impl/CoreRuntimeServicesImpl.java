/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import com.upo.orchestrator.engine.InputValueResolver;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Named("coreRuntimeServicesImpl")
@Singleton
public class CoreRuntimeServicesImpl extends ProcessServicesImpl {
  @Inject
  public CoreRuntimeServicesImpl(DefaultInputValueResolver inputValueResolver) {
    registerService(InputValueResolver.class, inputValueResolver);
  }
}
