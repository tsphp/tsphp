package ch.tutteli.tsphp;

import ch.tutteli.tsphp.common.ICompiler;
import ch.tutteli.tsphp.common.ITSPHPAstAdaptor;
import ch.tutteli.tsphp.common.ITranslatorFactory;
import ch.tutteli.tsphp.common.TSPHPAstAdaptor;
import ch.tutteli.tsphp.parser.ParserFacade;
import ch.tutteli.tsphp.translators.php54.PHP54TranslatorFactory;
import ch.tutteli.tsphp.typechecker.TypeChecker;
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
