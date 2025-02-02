/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.value;

import static com.upo.orchestrator.engine.impl.value.GroovyScriptCompiler.evaluateScript;
import static com.upo.orchestrator.engine.impl.value.ValueParser.VAR_PATTERN;

import java.util.*;
import java.util.regex.Matcher;

import com.upo.orchestrator.engine.ResolvableValue;
import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.VariableContainer;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.utilities.ds.CollectionUtils;
import com.upo.utilities.ds.Pair;
import com.upo.utilities.json.path.JsonPath;

import groovy.lang.Script;

/** ResolvableValue implementation for Groovy expressions. */
public class ExpressionResolvableValue implements ResolvableValue {
  private final Map<String, JsonPath> variables;
  private final Script compiledScript;

  public ExpressionResolvableValue(String expression) {
    ProcessedExpressionResult result = processExpression(expression.trim());
    this.compiledScript = GroovyScriptCompiler.compileExpression(result.processedExpression);
    this.variables = result.variableMappings;
  }

  @Override
  public <T> T evaluate(ProcessInstance context) {
    VariableContainer variableContainer = context.getVariableContainer();
   // include keys with null values, this is to ensure groovy execution doesn't
   // fail with undefined key
    Map<String, Object> variables =
        CollectionUtils.transformValueInMap(this.variables, variableContainer::readVariable, true);
   //noinspection unchecked
    return (T) evaluateScript(compiledScript, variables);
  }

  @Override
  public Set<Pair<String, Variable.Type>> getVariableDependencies() {
    Set<Pair<String, Variable.Type>> result = new HashSet<>();
    for (JsonPath value : variables.values()) {
      result.add(VariableResolvableValue.fromJsonPath(value));
    }
    return result;
  }

  private ProcessedExpressionResult processExpression(String originalExpression) {
    Map<String, JsonPath> mappings = new LinkedHashMap<>();
    StringBuilder processed = new StringBuilder(originalExpression);
    Matcher matcher = VAR_PATTERN.matcher(originalExpression);
    int varCounter = 1;
    int offset = 0;

    while (matcher.find()) {
      String varPath = matcher.group(1);
      String varName = "var" + varCounter++;
      mappings.put(varName, JsonPath.create(varPath.trim()));

     // Replace the placeholder with the variable name
      int start = matcher.start() - offset;
      int end = matcher.end() - offset;
      processed.replace(start, end, varName);

     // Update offset for subsequent replacements
      offset += (matcher.end() - matcher.start()) - varName.length();
    }

    return new ProcessedExpressionResult(
        processed.toString(), Collections.unmodifiableMap(mappings));
  }

  private record ProcessedExpressionResult(
      String processedExpression, Map<String, JsonPath> variableMappings) {}
}
