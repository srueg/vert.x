/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vertx.java.deploy.impl.java;

import java.io.File;
import java.util.Collections;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * 
 * Classloader for dynamic .java source file compilation and loading.
 * 
 * @author Janne Hietam&auml;ki
 */
public class CompilingClassLoader extends ClassLoader {
  private final File sourceFile;
  private final MemoryFileManager fileManager;

  public CompilingClassLoader(ClassLoader loader, String sourceName) {
    super(loader);
    this.sourceFile = new File(sourceName).getAbsoluteFile();
    if(!this.sourceFile.canRead()) {
      throw new RuntimeException("File not found: " + sourceName);
    }
    try {
      DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
      JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
      StandardJavaFileManager standardFileManager = javaCompiler.getStandardFileManager(null, null, null);
      
      standardFileManager.setLocation(StandardLocation.SOURCE_PATH, Collections.singleton(sourceFile.getParentFile()));

      fileManager = new MemoryFileManager(standardFileManager);
      JavaFileObject javaFile = standardFileManager.getJavaFileForInput(StandardLocation.SOURCE_PATH, resolveMainClassName(), Kind.SOURCE);
      JavaCompiler.CompilationTask task = javaCompiler.getTask(null, fileManager, diagnostics, null, null, Collections.singleton(javaFile));      
      boolean valid = task.call();      
      
      for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
        System.out.println(d);
      }
      if (!valid) {
        throw new RuntimeException("Compilation failed!");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String resolveMainClassName() {
    String fileName = sourceFile.getName();
    return fileName.substring(0, fileName.length() - Kind.SOURCE.extension.length());
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    byte[] bytecode = fileManager.getCompiledClass(name);
    if (bytecode == null) {
      throw new ClassNotFoundException(name);
    }
    return defineClass(name, bytecode, 0, bytecode.length);
  }
}
