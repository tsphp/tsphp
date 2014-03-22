/*
 * This file is part of the TSPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp;

import ch.tsphp.common.ICompiler;
import ch.tsphp.common.ITSPHPAstAdaptor;
import ch.tsphp.common.ITranslatorFactory;
import ch.tsphp.common.TSPHPAstAdaptor;
import ch.tsphp.parser.ParserFacade;
import ch.tsphp.translators.php54.PHP54TranslatorFactory;
import ch.tsphp.typechecker.TypeChecker;

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

    public ICompiler create(ExecutorService executorService) {
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
