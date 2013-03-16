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
package ch.tutteli.tsphp.test;

import ch.tutteli.tsphp.Compiler;
import ch.tutteli.tsphp.CompilerInitialiser;
import ch.tutteli.tsphp.ParserFactory;
import ch.tutteli.tsphp.common.ICompiler;
import ch.tutteli.tsphp.common.ICompilerListener;
import ch.tutteli.tsphp.common.ITranslator;
import ch.tutteli.tsphp.common.ITranslatorFactory;
import ch.tutteli.tsphp.common.TSPHPAstAdaptor;
import ch.tutteli.tsphp.exceptions.CompilerException;
import ch.tutteli.tsphp.typechecker.TypeChecker;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import junit.framework.Assert;
import org.antlr.runtime.tree.TreeNodeStream;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author Robert Stoll <rstoll@tutteli.ch>
 */
public class CompilerTest
{

    final CountDownLatch lock = new CountDownLatch(1);

    @Test
    public void testAddInputAfterCompileWithoutReset() throws InterruptedException {

        ICompiler compiler = new CompilerInitialiser().create();
        compiler.compile();
        try {
            compiler.addCompilationUnit("test", "int $a;");
            Assert.fail("No compiler exception thrown. It should not be allowed to add compilation units "
                    + "during compilation.");
        } catch (CompilerException ex) {
        }
    }

    @Test
    public void testResetDuringCompilation() throws InterruptedException {

        ICompiler compiler = new CompilerInitialiser().create();
        compiler.compile();
        try {
            compiler.reset();
            Assert.fail("No compiler exception thrown. It should not be allowed to reset during compilation.");
        } catch (CompilerException ex) {
        }
    }

    @Test
    public void testGetExceptionsDuringCompilation() throws InterruptedException {

        ICompiler compiler = new CompilerInitialiser().create();
        compiler.compile();
        try {
            compiler.getExceptions();
            Assert.fail("No compiler exception thrown. It should not be allowed to get the exceptions during "
                    + "compilation.");
        } catch (CompilerException ex) {
        }
    }

    @Test
    public void testHasFoundErrorDuringCompilation() throws InterruptedException {

        ICompiler compiler = new CompilerInitialiser().create();
        compiler.compile();
        try {
            compiler.hasFoundError();
            Assert.fail("No compiler exception thrown. It should not be allowed to test whether errors were by found"
                    + " during compilation.");
        } catch (CompilerException ex) {
        }
    }

    @Test
    public void testBlackBoxCompiler() throws InterruptedException, IOException {

        ITranslator translator = Mockito.mock(ITranslator.class);

        Mockito.when(translator.translate(Mockito.any(TreeNodeStream.class)))
                .thenReturn("tata");

        ITranslatorFactory factory = Mockito.mock(ITranslatorFactory.class);
        Mockito.when(factory.build()).thenReturn(translator);

        Collection<ITranslatorFactory> translators = new ArrayDeque<>();
        translators.add(factory);

        ICompiler compiler = new Compiler(
                new TypeChecker(new TSPHPAstAdaptor()),
                new ParserFactory(new TSPHPAstAdaptor()),
                translators,
                1);
        compiler.registerCompilerListener(new ICompilerListener()
        {
            @Override
            public void afterCompilingCompleted() {
                lock.countDown();
            }
        });
        compiler.addCompilationUnit("test", "int $a;");
        compiler.compile();
        lock.await(2, TimeUnit.SECONDS);
        Assert.assertFalse(compiler.hasFoundError());

        Map<String, String> translations = compiler.getTranslations();
        Assert.assertEquals(1, translations.size());
        Assert.assertEquals("tata", translations.get("test").toString());
    }
}
