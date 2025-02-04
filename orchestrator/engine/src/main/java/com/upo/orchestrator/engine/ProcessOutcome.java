/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

/** Represents the final outcome of a process execution. */
public interface ProcessOutcome {

  record Success(Object result) implements ProcessOutcome {}

  record Failure(Object failure) implements ProcessOutcome {}
}
