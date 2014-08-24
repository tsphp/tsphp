/*
 * This file is part of the TSPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp.test.unit;

import ch.tsphp.Compiler;
import ch.tsphp.common.ICompiler;
import ch.tsphp.common.IErrorLogger;
import ch.tsphp.common.IParser;
import ch.tsphp.common.ITSPHPAstAdaptor;
import ch.tsphp.common.ITranslatorFactory;
import ch.tsphp.common.ITypeChecker;
import ch.tsphp.common.exceptions.TSPHPException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CompilerTest
{

    protected ITSPHPAstAdaptor astAdaptor;
    protected IParser parser;
    protected ITypeChecker typeChecker;
    protected Collection<ITranslatorFactory> translatorFactories;
    protected ExecutorService executorService;


    @Test
    public void log_NoErrorLoggers_HasFoundErrorIsTrue() {
        //no arrange necessary

        ICompiler compiler = createCompiler();
        compiler.log(new TSPHPException());

        assertThat(compiler.hasFoundError(), is(true));
    }

    @Test
    public void log_Standard_HasFoundErrorIsTrue() {
        //no arrange necessary

        ICompiler compiler = createCompiler();
        compiler.registerErrorLogger(mock(IErrorLogger.class));
        compiler.log(new TSPHPException());

        assertThat(compiler.hasFoundError(), is(true));
    }

    @Test
    public void registerErrorLogger_Standard_IsInformedIfSomethingIsLogged() {
        IErrorLogger logger = mock(IErrorLogger.class);
        TSPHPException exception = new TSPHPException();

        ICompiler compiler = createCompiler();
        compiler.registerErrorLogger(logger);
        compiler.log(exception);

        verify(logger).log(exception);
    }

    protected ICompiler createCompiler() {
        astAdaptor = mock(ITSPHPAstAdaptor.class);
        parser = mock(IParser.class);
        typeChecker = mock(ITypeChecker.class);
        translatorFactories = new ArrayList<>();
        executorService = mock(ExecutorService.class);
        return new Compiler(astAdaptor, parser, typeChecker, translatorFactories, executorService);
    }
}
