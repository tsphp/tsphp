/*
 * This file is part of the TSPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp.test.integration;

import ch.tsphp.common.ICompiler;
import ch.tsphp.test.testutils.ACompilerTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class CompilerInputTest extends ACompilerTest
{

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testStringInput() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        compiler.addCompilationUnit("test", "int $a;");
        compileAndCheck(compiler, "test", "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testCharInput() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        char[] chars = "int $a;".toCharArray();
        compiler.addCompilationUnit("test", chars, chars.length);
        compileAndCheck(compiler, "test", "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testInputStream() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        InputStream stream = new ByteArrayInputStream("int $a;".getBytes(StandardCharsets.UTF_8));
        compiler.addCompilationUnit("test", stream);
        compileAndCheck(compiler, "test", "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testInputStreamIncludingEncoding() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        InputStream stream = new ByteArrayInputStream("int $a;".getBytes(StandardCharsets.UTF_8));
        compiler.addCompilationUnit("test", stream, "UTF-8");
        compileAndCheck(compiler, "test", "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testInputStreamIncludingInitialBufferSize() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        String testString = "int $a;";
        InputStream stream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));
        compiler.addCompilationUnit("test", stream, 1024);
        compileAndCheck(compiler, "test", "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testInputStreamIncludingInitialBufferSizeAndEncoding() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        String testString = "int $a;";
        InputStream stream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));
        compiler.addCompilationUnit("test", stream, 1024, "UTF-8");
        compileAndCheck(compiler, "test", "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testInputStreamIncludingInitialBufferSizeReadingBufferSizeAndEncoding()
            throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        String testString = "int $a;";
        InputStream stream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));
        compiler.addCompilationUnit("test", stream, 16, 16, "UTF-8");
        compileAndCheck(compiler, "test", "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testAddFile() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        File file = folder.newFile("test.tsphp");
        PrintWriter writer = new PrintWriter(file);
        writer.println("int $a;");
        writer.close();

        compiler.addFile(file.getAbsolutePath());
        compileAndCheck(compiler, file.getAbsolutePath(), "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testAddFileIncludingEncoding() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        File file = folder.newFile("test.tsphp");
        PrintWriter writer = new PrintWriter(file, "UTF-8");
        writer.println("int $a;");
        writer.close();

        compiler.addFile(file.getAbsolutePath(), "UTF-8");
        compileAndCheck(compiler, file.getAbsolutePath(), "<?php\nnamespace{\n    $a;\n}\n?>");
    }
}
