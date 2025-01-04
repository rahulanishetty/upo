/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl;

/**
 * Generic evaluator interface for applying filters to a record type. This interface provides a
 * flexible way to evaluate filter conditions against any type of record, allowing for type-safe
 * filter implementations across different data structures.
 *
 * @param <Record> the type of record this evaluator can process
 */
public interface FilterEvaluator<Record> {

  /**
   * Evaluates a record against configured filter conditions.
   *
   * @param record the record to evaluate
   * @return true if the record matches all filter conditions, false otherwise
   */
  boolean evaluate(Record record);
}
