/*
 * This file is part of the TSPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp.test.integration;

import ch.tsphp.Compiler;
import ch.tsphp.common.ICompiler;
import ch.tsphp.common.IErrorLogger;
import ch.tsphp.common.IParser;
import ch.tsphp.common.ITSPHPAstAdaptor;
import ch.tsphp.common.ITranslatorFactory;
import ch.tsphp.common.ITypeChecker;
import ch.tsphp.common.TSPHPAst;
import ch.tsphp.common.TSPHPAstAdaptor;
import ch.tsphp.common.exceptions.TSPHPException;
import ch.tsphp.parser.ParserFacade;
import ch.tsphp.test.testutils.ACompilerTest;
import ch.tsphp.typechecker.TypeChecker;
import org.antlr.runtime.tree.TreeNodeStream;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collection;
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

public class CompilerErrorTest extends ACompilerTest
{
    private CountDownLatch lock = new CountDownLatch(1);

    @Test
    public void testLogUnexpectedExceptionDuringParsingPhase() throws InterruptedException {
        IErrorLogger logger = mock(IErrorLogger.class);
        RuntimeException exception = new RuntimeException();
        IParser parser = spy(new ParserFacade());
        when(parser.parse(anyString())).thenThrow(exception);

        ICompiler compiler = new ch.tsphp.Compiler(
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
}
