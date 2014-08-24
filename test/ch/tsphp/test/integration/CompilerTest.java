/*
 * This file is part of the TSPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp.test.integration;

import ch.tsphp.Compiler;
import ch.tsphp.HardCodedCompilerInitialiser;
import ch.tsphp.common.ACompilerListener;
import ch.tsphp.common.ICompiler;
import ch.tsphp.common.IErrorLogger;
import ch.tsphp.common.IParser;
import ch.tsphp.common.ITSPHPAstAdaptor;
import ch.tsphp.common.ITranslatorFactory;
import ch.tsphp.common.ITypeChecker;
import ch.tsphp.common.ParserUnitDto;
import ch.tsphp.common.TSPHPAst;
import ch.tsphp.common.TSPHPAstAdaptor;
import ch.tsphp.common.exceptions.TSPHPException;
import ch.tsphp.exceptions.CompilerException;
import ch.tsphp.parser.ParserFacade;
import ch.tsphp.translators.php54.PHP54TranslatorFactory;
import ch.tsphp.typechecker.TypeChecker;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.TreeNodeStream;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompilerTest
{

    private CountDownLatch lock = new CountDownLatch(1);

    @Test
    public void addCompilationUnit_AfterCompileWithoutReset_ThrowsCompilerException() throws InterruptedException {

        ICompiler compiler = new HardCodedCompilerInitialiser().create();
        compiler.compile();
        try {
            compiler.addCompilationUnit("test", "int $a;");
            Assert.fail("No compiler exception thrown. It should not be allowed to add compilation units "
                    + "during compilation.");
        } catch (CompilerException ex) {
        }
    }

    @Test
    public void reset_DuringCompilation_ThrowsCompilerException() throws InterruptedException {

        ICompiler compiler = createSlowCompiler();
        compiler.compile();
        try {
            compiler.reset();
            Assert.fail("No compiler exception thrown. It should not be allowed to reset during compilation.");
        } catch (CompilerException ex) {
        }
    }

    @Test
    public void needsAReset_BeforeCompilation_ReturnsFalse() throws InterruptedException {
        //no arrange needed

        ICompiler compiler = createCompiler();
        boolean result = compiler.needsAReset();

        assertThat(result, is(false));
    }

    @Test
    public void needsAReset_AfterCompilation_ReturnsTrue() throws InterruptedException {
        //no arrange needed

        ICompiler compiler = createCompiler();
        compiler.compile();
        boolean result = compiler.needsAReset();

        assertThat(result, is(true));
    }

    @Test
    public void isCompiling_DuringCompilation_ReturnsTrue() throws InterruptedException {

        ICompiler compiler = createSlowCompiler();
        compiler.compile();
        boolean result = compiler.isCompiling();

        assertThat(result, is(true));
    }

    @Test
    public void isCompiling_NotDuringCompilation_ReturnsFalse() throws InterruptedException {

        ICompiler compiler = createCompiler();
        boolean result = compiler.isCompiling();

        assertThat(result, is(false));
    }

    @Test
    public void hasFoundError_DuringCompilation_ThrowsCompilerException() throws InterruptedException {
        ICompiler compiler = createSlowCompiler();
        compiler.addCompilationUnit("test", "int $a = 1");
        compiler.compile();
        try {
            compiler.hasFoundError();
            Assert.fail("No compiler exception thrown. It should not be allowed to test whether errors were found"
                    + " during compilation.");
        } catch (CompilerException ex) {
        }
    }

    @Test
    public void compile_DuringCompilation_ThrowsCompilerException() throws InterruptedException {

        ICompiler compiler = createSlowCompiler();
        compiler.compile();
        try {
            compiler.compile();
            Assert.fail("No compiler exception thrown. It should not be allowed to compile during compilation.");
        } catch (CompilerException ex) {
        }
    }

    @Test
    public void testBlackBoxStringInput() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        compiler.addCompilationUnit("test", "int $a;");
        compileAndCheck(compiler, "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testBlackBoxCharInput() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        char[] chars = "int $a;".toCharArray();
        compiler.addCompilationUnit("test", chars, chars.length);
        compileAndCheck(compiler, "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testBlackBoxInputStreamInput() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        InputStream stream = new ByteArrayInputStream("int $a;".getBytes(StandardCharsets.UTF_8));
        compiler.addCompilationUnit("test", stream);
        compileAndCheck(compiler, "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testBlackBoxInputStreamInputIncludingEncoding() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        InputStream stream = new ByteArrayInputStream("int $a;".getBytes(StandardCharsets.UTF_8));
        compiler.addCompilationUnit("test", stream, "UTF-8");
        compileAndCheck(compiler, "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testBlackBoxInputStreamInputIncludingSize() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        String testString = "int $a;";
        InputStream stream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));
        compiler.addCompilationUnit("test", stream, 1024);
        compileAndCheck(compiler, "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testBlackBoxInputStreamInputIncludingSizeAndEncoding() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        String testString = "int $a;";
        InputStream stream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));
        compiler.addCompilationUnit("test", stream, 1024, "UTF-8");
        compileAndCheck(compiler, "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testBlackBoxInputStreamInputIncludingBufferSizeReadingBufferSizeAndEncoding()
            throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        String testString = "int $a;";
        InputStream stream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));
        compiler.addCompilationUnit("test", stream, 16, 16, "UTF-8");
        compileAndCheck(compiler, "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testResetAndCompile() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        compiler.addCompilationUnit("test", "int $a;");
        compileAndCheck(compiler, "<?php\nnamespace{\n    $a;\n}\n?>");
        compiler.reset();
        lock = new CountDownLatch(1);
        compiler.addCompilationUnit("test", "int $a = 1;");
        compileAndCheck(compiler, "<?php\nnamespace{\n    $a = 1;\n}\n?>");
    }

    @Test
    public void testLogUnexpectedExceptionDuringParsingPhase() throws InterruptedException {
        IErrorLogger logger = mock(IErrorLogger.class);
        RuntimeException exception = new RuntimeException();
        IParser parser = spy(new ParserFacade());
        when(parser.parse(anyString())).thenThrow(exception);

        ICompiler compiler = new Compiler(
                mock(ITSPHPAstAdaptor.class),
                parser,
                mock(ITypeChecker.class),
                new ArrayList<ITranslatorFactory>(),
                Executors.newSingleThreadExecutor());
        compiler.registerErrorLogger(logger);
        compiler.addCompilationUnit("test", "int $a;");

        lock.await(500, TimeUnit.MILLISECONDS);
        assertThat(compiler.hasFoundError(), is(true));
        ArgumentCaptor<TSPHPException> captor = ArgumentCaptor.forClass(TSPHPException.class);
        verify(logger).log(captor.capture());
        assertThat(captor.getValue().getCause(), is((Throwable) exception));
    }

    @Test
    public void testLogUnexpectedExceptionDuringDefinitionPhase() throws InterruptedException {
        IErrorLogger logger = mock(IErrorLogger.class);
        ITypeChecker typeChecker = spy(new TypeChecker());
        RuntimeException exception = new RuntimeException();
        doThrow(exception).when(typeChecker).enrichWithDefinitions(any(TSPHPAst.class), any(TreeNodeStream.class));

        ICompiler compiler = new Compiler(
                new TSPHPAstAdaptor(),
                new ParserFacade(),
                typeChecker,
                new ArrayList<ITranslatorFactory>(),
                Executors.newSingleThreadExecutor());
        compiler.registerErrorLogger(logger);
        compiler.addCompilationUnit("test", "int $a;");
        compiler.compile();
        lock.await(500, TimeUnit.MILLISECONDS);

        assertThat(compiler.hasFoundError(), is(true));
        ArgumentCaptor<TSPHPException> captor = ArgumentCaptor.forClass(TSPHPException.class);
        //two since one is about the abortion of the compilation process - translation is not done due to errors
        verify(logger, times(2)).log(captor.capture());
        assertThat(captor.getAllValues().get(0).getCause(), is((Throwable) exception));
    }

    @Test
    public void testLogUnexpectedExceptionDuringReferencePhase() throws InterruptedException {
        IErrorLogger logger = mock(IErrorLogger.class);
        ITypeChecker typeChecker = spy(new TypeChecker());
        RuntimeException exception = new RuntimeException();
        doThrow(exception).when(typeChecker).enrichWithReferences(any(TSPHPAst.class), any(TreeNodeStream.class));

        ICompiler compiler = new Compiler(
                new TSPHPAstAdaptor(),
                new ParserFacade(),
                typeChecker,
                new ArrayList<ITranslatorFactory>(),
                Executors.newSingleThreadExecutor());
        compiler.registerErrorLogger(logger);
        compiler.addCompilationUnit("test", "int $a;");
        compiler.compile();
        lock.await(500, TimeUnit.MILLISECONDS);

        assertThat(compiler.hasFoundError(), is(true));
        ArgumentCaptor<TSPHPException> captor = ArgumentCaptor.forClass(TSPHPException.class);
        //two since one is about the abortion of the compilation process - translation is not done due to errors
        verify(logger, times(2)).log(captor.capture());
        assertThat(captor.getAllValues().get(0).getCause(), is((Throwable) exception));
    }

    @Test
    public void testLogUnexpectedExceptionDuringTypeCheckingPhase() throws InterruptedException {
        IErrorLogger logger = mock(IErrorLogger.class);
        ITypeChecker typeChecker = spy(new TypeChecker());
        RuntimeException exception = new RuntimeException();
        doThrow(exception).when(typeChecker).doTypeChecking(any(TSPHPAst.class), any(TreeNodeStream.class));

        ICompiler compiler = new Compiler(
                new TSPHPAstAdaptor(),
                new ParserFacade(),
                typeChecker,
                new ArrayList<ITranslatorFactory>(),
                Executors.newSingleThreadExecutor());
        compiler.registerErrorLogger(logger);
        compiler.addCompilationUnit("test", "int $a;");
        compiler.compile();
        lock.await(500, TimeUnit.MILLISECONDS);

        assertThat(compiler.hasFoundError(), is(true));
        ArgumentCaptor<TSPHPException> captor = ArgumentCaptor.forClass(TSPHPException.class);
        //two since one is about the abortion of the compilation process - translation is not done due to errors
        verify(logger, times(2)).log(captor.capture());
        assertThat(captor.getAllValues().get(0).getCause(), is((Throwable) exception));
    }

    @Test
    public void testLogUnexpectedExceptionDuringTranslatorPhase() throws InterruptedException {
        IErrorLogger logger = mock(IErrorLogger.class);
        RuntimeException exception = new RuntimeException();
        ITranslatorFactory translatorFactory = mock(ITranslatorFactory.class);
        when(translatorFactory.build()).thenThrow(exception);
        Collection<ITranslatorFactory> translatorFactories = new ArrayList<>();
        translatorFactories.add(translatorFactory);

        ICompiler compiler = new Compiler(
                new TSPHPAstAdaptor(),
                new ParserFacade(),
                new TypeChecker(),
                translatorFactories,
                Executors.newSingleThreadExecutor());
        compiler.registerErrorLogger(logger);
        compiler.addCompilationUnit("test", "int $a;");
        compiler.compile();
        lock.await(500, TimeUnit.MILLISECONDS);

        assertThat(compiler.hasFoundError(), is(true));
        ArgumentCaptor<TSPHPException> captor = ArgumentCaptor.forClass(TSPHPException.class);
        verify(logger).log(captor.capture());
        assertThat(captor.getValue().getCause(), is((Throwable) exception));
    }

    @Test
    public void testLogWhenNoTranslatorFactoryIsProvided() throws InterruptedException {
        IErrorLogger logger = mock(IErrorLogger.class);

        ICompiler compiler = new Compiler(
                new TSPHPAstAdaptor(),
                new ParserFacade(),
                new TypeChecker(),
                null,
                Executors.newSingleThreadExecutor());
        compiler.registerErrorLogger(logger);
        compiler.addCompilationUnit("test", "int $a;");
        compiler.compile();
        lock.await(500, TimeUnit.MILLISECONDS);

        assertThat(compiler.hasFoundError(), is(true));
        verify(logger).log(any(TSPHPException.class));
    }

    protected ICompiler createCompiler() {
        ICompiler compiler = new HardCodedCompilerInitialiser().create();
        compiler.registerCompilerListener(new ACompilerListener()
        {
            @Override
            public void afterCompilingCompleted() {
                lock.countDown();
            }
        });
        return compiler;
    }

    private void compileAndCheck(ICompiler compiler, String translation) throws InterruptedException {

        compiler.compile();
        lock.await(1000, TimeUnit.MILLISECONDS);
        Assert.assertFalse(compiler.hasFoundError());

        Map<String, String> translations = compiler.getTranslations();
        assertThat(translations.size(), is(1));
        assertThat(translations.get("test").replaceAll("\r", ""), is(translation));
    }

    private ICompiler createSlowCompiler() {
        Collection<ITranslatorFactory> translatorFactories = new ArrayDeque<>();
        translatorFactories.add(new PHP54TranslatorFactory());

        ITSPHPAstAdaptor adaptor = new TSPHPAstAdaptor();
        IParser mockParser = mock(IParser.class);

        when(mockParser.parse(Mockito.anyString())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(2000);
                return new ParserUnitDto("bla", new TSPHPAst(), new CommonTokenStream());
            }
        });
        return new Compiler(
                adaptor,
                mockParser,
                new TypeChecker(),
                translatorFactories,
                Executors.newSingleThreadExecutor());
    }
}
