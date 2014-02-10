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

public class HardCodedCompilerInitialiser implements ICompilerInitialiser
{

    private static final int CORE_MULTIPLICATION_FACTOR = 4;

    @Override
    public ICompiler create() {
        return create(Runtime.getRuntime().availableProcessors() * CORE_MULTIPLICATION_FACTOR);
    }

    @Override
    public ICompiler create(final int numberOfWorkers) {
        Collection<ITranslatorFactory> translatorFactories = new ArrayDeque<>();
        translatorFactories.add(new PHP54TranslatorFactory());

        ITSPHPAstAdaptor adaptor = new TSPHPAstAdaptor();
        return new Compiler(
                adaptor,
                new ParserFacade(adaptor),
                new TypeChecker(),
                translatorFactories,
                numberOfWorkers);
    }
}
