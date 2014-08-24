/*
 * This file is part of the TSPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp.test.testutils;

import ch.tsphp.HardCodedCompilerInitialiser;
import ch.tsphp.common.ACompilerListener;
import ch.tsphp.common.ICompiler;
import ch.tsphp.common.IParser;
import ch.tsphp.common.ITSPHPAstAdaptor;
import ch.tsphp.common.ITranslatorFactory;
import ch.tsphp.common.ParserUnitDto;
import ch.tsphp.common.TSPHPAst;
import ch.tsphp.common.TSPHPAstAdaptor;
import ch.tsphp.translators.php54.PHP54TranslatorFactory;
import ch.tsphp.typechecker.TypeChecker;
import org.antlr.runtime.CommonTokenStream;
import org.junit.Assert;
import org.junit.Ignore;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class ACompilerTest
{
    protected CountDownLatch lock = new CountDownLatch(1);

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

    protected void compileAndCheck(ICompiler compiler, String id, String translation) throws InterruptedException {
        compiler.compile();
        lock.await(2, TimeUnit.SECONDS);

        Assert.assertFalse(compiler.hasFoundError());

        Map<String, String> translations = compiler.getTranslations();
        assertThat(translations.size(), is(1));
        assertThat(translations.get(id).replaceAll("\r", ""), is(translation));
    }

    protected ICompiler createSlowCompiler() {
        Collection<ITranslatorFactory> translatorFactories = new ArrayDeque<>();
        translatorFactories.add(new PHP54TranslatorFactory());

        ITSPHPAstAdaptor adaptor = new TSPHPAstAdaptor();
        IParser mockParser = mock(IParser.class);

        when(mockParser.parse(Mockito.anyString())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(2000);
                return new ParserUnitDto("dummy", new TSPHPAst(), new CommonTokenStream());
            }
        });
        return new ch.tsphp.Compiler(
                adaptor,
                mockParser,
                new TypeChecker(),
                translatorFactories,
                Executors.newSingleThreadExecutor());
    }
}
