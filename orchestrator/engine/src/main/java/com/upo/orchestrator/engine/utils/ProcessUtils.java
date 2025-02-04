/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.utils;

import com.upo.orchestrator.engine.models.ProcessInstance;

public class ProcessUtils {

  public static boolean isRootInstance(ProcessInstance processInstance) {
    return processInstance.getRootId() == null;
  }

  public static String getRootInstanceId(ProcessInstance processInstance) {
    if (isRootInstance(processInstance)) {
      return processInstance.getId();
    }
    return processInstance.getRootId();
  }
}
