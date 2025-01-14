/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.upo.orchestrator.engine.ResolvableValue;

import jakarta.inject.Singleton;

/**
 * Parses input strings containing variable references and expressions into ResolvableValue
 * components. Handles: - Static text - Variable references: {{taskId.input/output.path}} - Groovy
 * expressions: [[expression]]
 */
@Singleton
public final class ValueParser {

  public static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{([^}]+)}}");
  private static final Pattern EXPR_PATTERN = Pattern.compile("\\[\\[(.+?)]]");

  /**
   * Parses an input string into a list of ResolvableValue components.
   *
   * @param input The input string to parse
   * @return List of ResolvableValue components
   */
  public static List<ResolvableValue> parse(String input) {
    if (input == null) {
      return Collections.singletonList(new StaticResolvableValue(null));
    }
    List<ResolvableValue> parts = new ArrayList<>();
    Matcher varMatcher = VAR_PATTERN.matcher(input);
    Matcher exprMatcher = EXPR_PATTERN.matcher(input);
    int currentPos = 0;
    while (currentPos < input.length()) {
      boolean foundVar = varMatcher.find(currentPos);
      boolean foundExpr = exprMatcher.find(currentPos);
      if (!foundVar && !foundExpr) {
       // Add remaining text as static value
        parts.add(new StaticResolvableValue(input.substring(currentPos)));
        break;
      }
     // Determine which pattern comes first
      int varStart = foundVar ? varMatcher.start() : input.length();
      int exprStart = foundExpr ? exprMatcher.start() : input.length();

      if (varStart < exprStart) {
       // Handle variable reference
        if (currentPos < varStart) {
          parts.add(new StaticResolvableValue(input.substring(currentPos, varStart)));
        }
        parts.add(new VariableResolvableValue(varMatcher.group(1)));
        currentPos = varMatcher.end();
      } else {
       // Handle expression
        if (currentPos < exprStart) {
          parts.add(new StaticResolvableValue(input.substring(currentPos, exprStart)));
        }
        parts.add(new ExpressionResolvableValue(exprMatcher.group(1)));
        currentPos = exprMatcher.end();
      }
    }
    return parts;
  }
}
