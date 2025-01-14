/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.value;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.codehaus.groovy.runtime.InvokerHelper;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Handles compilation and execution of Groovy expressions with caching and security restrictions.
 */
public final class GroovyScriptCompiler {
  private static final GroovyShell SHELL;

  static {
    CompilerConfiguration config = createSecureCompilerConfiguration();
    SHELL = new GroovyShell(config);
  }

 // Prevent instantiation
  private GroovyScriptCompiler() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Creates a secure configuration for Groovy script compilation. Restricts available imports and
   * operations for security.
   */
  private static CompilerConfiguration createSecureCompilerConfiguration() {
    CompilerConfiguration config = new CompilerConfiguration();

   // Configure allowed imports
    ImportCustomizer imports = new ImportCustomizer();
    imports.addStaticStars("java.lang.Math");
    config.addCompilationCustomizers(imports);

   // Configure security restrictions
    SecureASTCustomizer secure = new SecureASTCustomizer();
    secure.setClosuresAllowed(true);
    secure.setMethodDefinitionAllowed(false);

   // Define allowed packages
    List<String> allowedPackages = Arrays.asList("java.lang", "java.util", "java.math");
    secure.setPackageAllowed(true);
    secure.setAllowedImports(allowedPackages.stream().map(pkg -> pkg + ".*").toList());

    config.addCompilationCustomizers(secure);
    return config;
  }

  /**
   * Compiles a Groovy expression using the shared shell instance. Thread-safe due to GroovyShell's
   * internal synchronization.
   *
   * @param expression The Groovy expression to compile
   * @return Compiled Script object
   */
  public static Script compileExpression(String expression) {
    return SHELL.parse(expression.trim());
  }

  /**
   * Evaluates a compiled script with the given variable bindings. Creates a clone of the script for
   * thread-safe execution.
   */
  public static Object evaluateScript(Script script, Map<String, Object> variables) {
   // Create a clone of the script for thread-safe execution
    Script clonedScript = InvokerHelper.createScript(script.getClass(), new Binding(variables));
    try {
      return clonedScript.run();
    } finally {
      clonedScript.setBinding(null);
    }
  }
}
