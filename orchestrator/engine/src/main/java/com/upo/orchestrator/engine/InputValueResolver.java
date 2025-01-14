/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

/**
 * Resolves raw input objects into evaluable expressions. This interface handles the initial parsing
 * and structuring of input values, converting them into a form that can be evaluated at runtime.
 */
public interface InputValueResolver {
  /**
   * Resolves an input object into a ResolvableValue.
   *
   * @param input The raw input object to resolve. Can be: - String containing expressions:
   *     "[[groovy-expr]] with {{variables}}" - Map/List of values to be resolved - Primitive values
   *     (passed through as static ResolvableValues)
   * @return A ResolvableValue that can be evaluated at runtime
   */
  ResolvableValue resolve(Object input);
}
