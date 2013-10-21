package ch.tutteli.tsphp;

import ch.tutteli.tsphp.common.CompilationUnitDto;
import ch.tutteli.tsphp.common.ICompiler;
import ch.tutteli.tsphp.common.ICompilerListener;
import ch.tutteli.tsphp.common.IErrorLogger;
import ch.tutteli.tsphp.common.IParser;
import ch.tutteli.tsphp.common.ITSPHPAstAdaptor;
import ch.tutteli.tsphp.common.ITranslator;
import ch.tutteli.tsphp.common.ITranslatorFactory;
import ch.tutteli.tsphp.common.ITypeChecker;
import ch.tutteli.tsphp.common.ParserUnitDto;
import ch.tutteli.tsphp.common.exceptions.TSPHPException;
import ch.tutteli.tsphp.exceptions.CompilerException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.antlr.runtime.tree.CommonTreeNodeStream;

public class Compiler implements ICompiler
{

    private final ITSPHPAstAdaptor astAdaptor;
    private final IParser parser;
    private final ITypeChecker typeChecker;
    private final ExecutorService executorService;
    //
    private final Collection<ICompilerListener> compilerListeners = new ArrayDeque<>();
    private final Collection<ITranslatorFactory> translatorFactories;
    //
    private Collection<CompilationUnitDto> compilationUnits = new ArrayDeque<>();
    private final Collection<IErrorLogger> errorLoggers = new ArrayDeque<>();
    private boolean isCompiling = false;
    private boolean needReset = false;
    private boolean hasFoundError = false;
    //
    private final Object lock = new Object();
    private Map<String, String> translations = new HashMap<>();
    private Collection<Future> tasks = new ArrayDeque<>();

    public Compiler(ITSPHPAstAdaptor theAstAdaptor, IParser theParser, ITypeChecker theTypeChecker,
            Collection<ITranslatorFactory> theTranslatorFactories, int numberOfWorkers) {
        astAdaptor = theAstAdaptor;
        typeChecker = theTypeChecker;
        parser = theParser;
        translatorFactories = theTranslatorFactories;
        executorService = Executors.newFixedThreadPool(numberOfWorkers);

        init();
    }

    private void init() {
        parser.registerErrorLogger(this);
        typeChecker.registerErrorLogger(this);
    }

    @Override
    public void registerCompilerListener(ICompilerListener listener) {
        compilerListeners.add(listener);
    }

    @Override
    public boolean hasFoundError() {
        boolean hasStartedCompiling;
        synchronized (lock) {
            hasStartedCompiling = isCompiling;
        }
        if (hasStartedCompiling) {
            throw new CompilerException("Cannot check for exceptions during compilation.");
        }
        return hasFoundError;
    }

    @Override
    public void registerErrorLogger(IErrorLogger errorLogger) {
        errorLoggers.add(errorLogger);
    }

    @Override
    public void log(TSPHPException exception) {
        hasFoundError = true;
        for (IErrorLogger logger : errorLoggers) {
            logger.log(exception);
        }
    }

    @Override
    public void addCompilationUnit(String id, final String string) {
        add(new ParseAndDefinitionPhaseRunner(id, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) {
                return theParser.parse(string);
            }
        }));
    }

    private void add(ParseAndDefinitionPhaseRunner runner) {
        boolean doesNotNetReset;
        synchronized (lock) {
            doesNotNetReset = !needReset;
        }
        if (doesNotNetReset) {
            tasks.add(executorService.submit(runner));
        } else {
            throw new CompilerException("Tried to parse after calling compile(). If compilation was finished "
                    + "and you wish to recompile, then use reset() first.");
        }
    }

    @Override
    public void addCompilationUnit(String id, final char[] chars, final int numberOfActualCharsInArray) {
        add(new ParseAndDefinitionPhaseRunner(id, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) {
                return theParser.parse(chars, numberOfActualCharsInArray);
            }
        }));
    }

    @Override
    public void addCompilationUnit(String id, final InputStream inputStream) throws IOException {
        add(new ParseAndDefinitionPhaseRunner(id, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) throws IOException {
                return theParser.parseInputStream(inputStream);
            }
        }));
    }

    @Override
    public void addCompilationUnit(String id, final InputStream inputStream, final int size) throws IOException {
        add(new ParseAndDefinitionPhaseRunner(id, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) throws IOException {
                return theParser.parseInputStream(inputStream, size);
            }
        }));
    }

    @Override
    public void addCompilationUnit(String id, final InputStream inputStream, final String encoding) throws IOException {
        add(new ParseAndDefinitionPhaseRunner(id, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) throws IOException {
                return theParser.parseInputStream(inputStream, encoding);
            }
        }));
    }

    @Override
    public void addCompilationUnit(String id, final InputStream inputStream, final int size, final String encoding)
            throws IOException {
        add(new ParseAndDefinitionPhaseRunner(id, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) throws IOException {
                return theParser.parseInputStream(inputStream, size, encoding);
            }
        }));
    }

    @Override
    public void addCompilationUnit(String id, final InputStream inputStream, final int size, final int readBufferSize,
            final String encoding) throws IOException {
        add(new ParseAndDefinitionPhaseRunner(id, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) throws IOException {
                return theParser.parseInputStream(inputStream, size, readBufferSize, encoding);
            }
        }));
    }

    @Override
    public void addFile(final String pathToFileInclFileName) throws IOException {
        add(new ParseAndDefinitionPhaseRunner(pathToFileInclFileName, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) throws IOException {
                return theParser.parseFile(pathToFileInclFileName);
            }
        }));
    }

    @Override
    public void addFile(final String pathToFileInclFileName, final String encoding) throws IOException {
        add(new ParseAndDefinitionPhaseRunner(pathToFileInclFileName, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) throws IOException {
                return theParser.parseFile(pathToFileInclFileName, encoding);
            }
        }));
    }

    @Override
    public void compile() {
        boolean doesNotNeedReset;
        synchronized (lock) {
            doesNotNeedReset = !needReset;
            isCompiling = true;
            needReset = true;
        }
        if (doesNotNeedReset) {
            waitUntilExecutorFinished(new Runnable()
            {
                @Override
                public void run() {
                    doReferencePhase();
                }
            });
        } else {
            throw new CompilerException("Cannot compile during an ongoing compilation.");
        }
    }

    @Override
    public boolean isCompiling() {
        synchronized (lock) {
            return isCompiling;
        }
    }

    @Override
    public boolean needsAReset() {
        synchronized (lock) {
            return needReset;
        }
    }

    @Override
    public void reset() {
        boolean hasStartedCompiling;
        synchronized (lock) {
            hasStartedCompiling = isCompiling;
        }
        if (hasStartedCompiling) {
            throw new CompilerException("Cannot reset during compilation.");
        }
        typeChecker.reset();
        parser.reset();
        compilationUnits = new ArrayDeque<>();
        translations = new HashMap<>();
        hasFoundError = false;
        needReset = false;

    }

    private void waitUntilExecutorFinished(final Runnable callback) {
        new Thread(new Runnable()
        {
            @Override
            public void run() {
                try {
                    for (Future task : tasks) {
                        task.get();
                    }
                    tasks = new ArrayDeque<>();
                    callback.run();
                } catch (Exception ex) {
                    log(new TSPHPException("Unexpected exception occurred: " + ex.getMessage(), ex));
                }
            }
        }).start();

    }

    private void doReferencePhase() {
        informParsingDefinitionCompleted();
        if (!compilationUnits.isEmpty()) {
            for (CompilationUnitDto compilationUnit : compilationUnits) {
                tasks.add(executorService.submit(new ReferencePhaseRunner(compilationUnit)));
            }
            waitUntilExecutorFinished(new Runnable()
            {
                @Override
                public void run() {
                    doTypeChecking();
                }
            });
        } else {
            log(new TSPHPException("No compilation units specified"));
            informCompilingCompleted();
        }
    }

    private void doTypeChecking() {
        informReferenceCompleted();
        if (!compilationUnits.isEmpty()) {
            for (CompilationUnitDto compilationUnit : compilationUnits) {
                tasks.add(executorService.submit(new TypeCheckRunner(compilationUnit)));
            }
            waitUntilExecutorFinished(new Runnable()
            {
                @Override
                public void run() {
                    doTranslation();
                }
            });
        } else {
            log(new TSPHPException("No compilation units specified"));
            informCompilingCompleted();
        }
    }

    private void doTranslation() {
        informTypeCheckingCompleted();
        if (!hasFoundError) {
            if (translatorFactories != null) {
                for (final ITranslatorFactory translatorFactory : translatorFactories) {
                    for (final CompilationUnitDto compilationUnit : compilationUnits) {
                        tasks.add(executorService.submit(new TranslatorRunner(translatorFactory, compilationUnit)));
                    }
                }
                waitUntilExecutorFinished(new Runnable()
                {
                    @Override
                    public void run() {
                        informCompilingCompleted();
                    }
                });
            } else {
                log(new TSPHPException("No translator factories specified"));
                informCompilingCompleted();
            }
        } else {
            log(new TSPHPException("Translation aborted due to occurred errors"));
            informCompilingCompleted();
        }
    }

    @Override
    public Map<String, String> getTranslations() {
        return translations;
    }

    private void informParsingDefinitionCompleted() {
        for (ICompilerListener listener : compilerListeners) {
            listener.afterParsingAndDefinitionPhaseCompleted();
        }
    }

    private void informReferenceCompleted() {
        for (ICompilerListener listener : compilerListeners) {
            listener.afterReferencePhaseCompleted();
        }
    }

    private void informTypeCheckingCompleted() {
        for (ICompilerListener listener : compilerListeners) {
            listener.afterTypecheckingCompleted();
        }
    }

    private void informCompilingCompleted() {
        isCompiling = false;
        for (ICompilerListener listener : compilerListeners) {
            listener.afterCompilingCompleted();
        }
    }

    /**
     * Delegate of a parser method which returns a ParserUnitDto.
     */
    private interface IParserMethod
    {

        ParserUnitDto parser(IParser parser) throws IOException;
    }

    private class ParseAndDefinitionPhaseRunner implements Runnable
    {

        private final IParserMethod parserMethod;
        private final String id;

        public ParseAndDefinitionPhaseRunner(String theId, IParserMethod aParserMethod) {
            parserMethod = aParserMethod;
            id = theId;
        }

        @Override
        public void run() {
            try {
                ParserUnitDto parserUnit = parserMethod.parser(parser);
                CommonTreeNodeStream commonTreeNodeStream = new CommonTreeNodeStream(
                        astAdaptor, parserUnit.compilationUnit);
                commonTreeNodeStream.setTokenStream(parserUnit.tokenStream);

                typeChecker.enrichWithDefinitions(parserUnit.compilationUnit, commonTreeNodeStream);
                compilationUnits.add(new CompilationUnitDto(id, parserUnit.compilationUnit, commonTreeNodeStream));

            } catch (Exception ex) {
                log(new TSPHPException("Unexpected exception occurred: " + ex.getMessage(), ex));
            }
        }
    }

    private class ReferencePhaseRunner implements Runnable
    {

        private final CompilationUnitDto dto;

        ReferencePhaseRunner(CompilationUnitDto aDto) {
            dto = aDto;
        }

        @Override
        public void run() {
            try {
                typeChecker.enrichWithReferences(dto.compilationUnit, dto.treeNodeStream);
            } catch (Exception ex) {
                log(new TSPHPException("Unexpected exception occurred: " + ex.getMessage(), ex));
            }
        }
    }

    private class TypeCheckRunner implements Runnable
    {

        private final CompilationUnitDto dto;

        TypeCheckRunner(CompilationUnitDto aDto) {
            dto = aDto;
        }

        @Override
        public void run() {
            try {
                typeChecker.doTypeChecking(dto.compilationUnit, dto.treeNodeStream);
            } catch (Exception ex) {
                log(new TSPHPException("Unexpected exception occurred: " + ex.getMessage(), ex));
            }
        }
    }

    private class TranslatorRunner implements Runnable
    {

        private final CompilationUnitDto dto;
        private final ITranslatorFactory translatorFactory;

        public TranslatorRunner(ITranslatorFactory theTranslatorFactory, CompilationUnitDto compilationUnit) {
            translatorFactory = theTranslatorFactory;
            dto = compilationUnit;
        }

        @Override
        public void run() {
            try {
                dto.treeNodeStream.reset();
                ITranslator translator = translatorFactory.build();
                translator.registerErrorLogger(Compiler.this);
                String translation = translator.translate(dto.treeNodeStream);
                translations.put(dto.id, translation);
            } catch (Exception ex) {
                log(new TSPHPException("Unexpected exception occurred: " + ex.getMessage(), ex));
            }
        }
    }
}
