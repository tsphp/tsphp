package ch.tsphp;

import ch.tsphp.common.ICompiler;
import ch.tsphp.common.ITSPHPAstAdaptor;
import ch.tsphp.common.ITranslatorFactory;
import ch.tsphp.common.TSPHPAstAdaptor;
import ch.tsphp.parser.ParserFacade;
import ch.tsphp.translators.php54.PHP54TranslatorFactory;
import ch.tsphp.typechecker.TypeChecker;

import java.lang.*;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HardCodedCompilerInitialiser implements ICompilerInitialiser
{

    private static final int CORE_MULTIPLICATION_FACTOR = 4;

    @Override
    public ICompiler create() {
        return create(Runtime.getRuntime().availableProcessors() * CORE_MULTIPLICATION_FACTOR);
    }

    @Override
    public ICompiler create(final int numberOfWorkers) {
       return create(Executors.newFixedThreadPool(numberOfWorkers));
    }

    public ICompiler create(ExecutorService executorService){
        Collection<ITranslatorFactory> translatorFactories = new ArrayDeque<>();
        translatorFactories.add(new PHP54TranslatorFactory());

        ITSPHPAstAdaptor adaptor = new TSPHPAstAdaptor();
        return new Compiler(
                adaptor,
                new ParserFacade(adaptor),
                new TypeChecker(),
                translatorFactories,
                executorService);
    }
}
