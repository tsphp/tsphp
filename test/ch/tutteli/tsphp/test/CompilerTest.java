package ch.tutteli.tsphp.test;

import ch.tutteli.tsphp.Compiler;
import ch.tutteli.tsphp.HardCodedCompilerInitialiser;
import ch.tutteli.tsphp.common.ACompilerListener;
import ch.tutteli.tsphp.common.ICompiler;
import ch.tutteli.tsphp.common.IParser;
import ch.tutteli.tsphp.common.ITSPHPAstAdaptor;
import ch.tutteli.tsphp.common.ITranslatorFactory;
import ch.tutteli.tsphp.common.ParserUnitDto;
import ch.tutteli.tsphp.common.TSPHPAst;
import ch.tutteli.tsphp.common.TSPHPAstAdaptor;
import ch.tutteli.tsphp.exceptions.CompilerException;
import ch.tutteli.tsphp.translators.php54.PHP54TranslatorFactory;
import ch.tutteli.tsphp.typechecker.TypeChecker;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import junit.framework.Assert;
import org.antlr.runtime.CommonTokenStream;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class CompilerTest
{

    private CountDownLatch lock = new CountDownLatch(1);

    @Test
    public void testAddInputAfterCompileWithoutReset() throws InterruptedException {

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
    public void testResetDuringCompilation() throws InterruptedException {

        ICompiler compiler = getSlowCompiler();
        compiler.compile();
        try {
            compiler.reset();
            Assert.fail("No compiler exception thrown. It should not be allowed to reset during compilation.");
        } catch (CompilerException ex) {
        }
    }

    @Test
    public void testHasFoundErrorDuringCompilation() throws InterruptedException {
        ICompiler compiler = getSlowCompiler();
        compiler.addCompilationUnit("test", "int $a = 1");
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

        ICompiler compiler = getCompiler();
        compiler.addCompilationUnit("test", "int $a;");
        compileAndCheck(compiler, "<?php\nnamespace{\n    $a;\n}\n?>");
    }

    @Test
    public void testResetAndCompile() throws InterruptedException, IOException {


        ICompiler compiler = getCompiler();
        compiler.addCompilationUnit("test", "int $a;");
        compileAndCheck(compiler, "<?php\nnamespace{\n    $a;\n}\n?>");
        compiler.reset();
        lock = new CountDownLatch(1);
        compiler.addCompilationUnit("test", "int $a = 1;");
        compileAndCheck(compiler, "<?php\nnamespace{\n    $a = 1;\n}\n?>");

    }

    private ICompiler getCompiler() {
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
        lock.await(2, TimeUnit.SECONDS);
        Assert.assertFalse(compiler.hasFoundError());

        Map<String, String> translations = compiler.getTranslations();
        Assert.assertEquals(1, translations.size());
        Assert.assertEquals(translation, translations.get("test").replaceAll("\r", ""));
    }

    private ICompiler getSlowCompiler() {
        Collection<ITranslatorFactory> translatorFactories = new ArrayDeque<>();
        translatorFactories.add(new PHP54TranslatorFactory());

        ITSPHPAstAdaptor adaptor = new TSPHPAstAdaptor();
        IParser spy = Mockito.mock(IParser.class);

        Mockito.when(spy.parse(Mockito.anyString())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(2000);
                return new ParserUnitDto("bla", new TSPHPAst(), new CommonTokenStream());
            }
        });
        ICompiler compiler = new Compiler(
                adaptor,
                spy,
                new TypeChecker(),
                translatorFactories,
                1);
        return compiler;
    }
}
