/*
 * Copyright 2012 Robert Stoll <rstoll@tutteli.ch>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package ch.tutteli.tsphp;

import ch.tutteli.tsphp.common.IErrorReporter;
import ch.tutteli.tsphp.common.ITranslator;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author Robert Stoll <rstoll@tutteli.ch>
 */
public interface ICompiler extends IErrorReporter
{

    void registerCompilerListener(ICompilerListener listener);

    void addCompilationUnit(String id, String inputString);

    void addCompilationUnit(String id, char[] data, int numberOfActualCharsInArray);

    void addCompilationUnit(String id, InputStream input) throws IOException;

    void addCompilationUnit(String id, InputStream input, int size) throws IOException;

    void addCompilationUnit(String id, InputStream input, String encoding) throws IOException;

    void addCompilationUnit(String id, InputStream input, int size, String encoding) throws IOException;

    void addCompilationUnit(String id, InputStream input, int size, int readBufferSize, String encoding)
            throws IOException;

    /**
     * Add the given file to the compilation units and use pathToFileInclFileName as identifier
     */
    void addFile(String pathToFileInclFileName) throws IOException;

    /**
     * Add the given file to the compilation units and use pathToFileInclFileName as identifier
     */
    void addFile(String pathToFileInclFileName, String encoding) throws IOException;

    void compile();

    Map<String, String> getTranslations();
}
