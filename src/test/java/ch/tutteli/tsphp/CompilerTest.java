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

import ch.tutteli.tsphp.common.ITSPHPAst;
import ch.tutteli.tsphp.common.ITranslator;
import ch.tutteli.tsphp.common.ITranslatorFactory;
import ch.tutteli.tsphp.common.TSPHPAstAdaptor;
import ch.tutteli.tsphp.exceptions.CompilerException;
import ch.tutteli.tsphp.typechecker.TypeChecker;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

/**
 *
 * @author Robert Stoll <rstoll@tutteli.ch>
 */
public class CompilerTest
{

    final CountDownLatch lock = new CountDownLatch(1);

    @Test
    public void testCompilerAddInputAfterCompiling() throws InterruptedException {

        ICompiler compiler = new CompilerInitialiser().create();
        compiler.registerCompilerListener(new ICompilerListener()
        {
            @Override
            public void afterCompilingCompleted() {
                lock.countDown();
            }
        });
        compiler.compile();
        compiler.addCompilationUnit("test", "int $a;");
        lock.await(2, TimeUnit.SECONDS);
        Assert.assertTrue(compiler.hasFoundError());
        List<Exception> exceptions = compiler.getExceptions();
        Assert.assertEquals(1, exceptions.size());
        Assert.assertTrue(exceptions.get(0) instanceof CompilerException);
    }

    @Test
    public void testBlackBoxCompiler() throws InterruptedException, IOException {

        ITranslator translator = Mockito.mock(ITranslator.class);
        ObjectOutputStream stream = null;
        try {
            stream = new ObjectOutputStream(new ByteArrayOutputStream());
        } catch (IOException ex) {
        }
        stream.writeChars("tata");

        Mockito.when(translator.translate(Mockito.any(ITSPHPAst.class))).thenReturn(stream);

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

        Map<String, OutputStream> outputStreams = compiler.getOutputStreams();
        Assert.assertEquals(1, outputStreams.size());
        Assert.assertEquals("tata", outputStreams.get("test")..toString());
    }
}
