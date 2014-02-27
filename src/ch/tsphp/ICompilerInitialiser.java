package ch.tsphp;

import ch.tsphp.common.ICompiler;

import java.util.concurrent.ExecutorService;

public interface ICompilerInitialiser
{

    ICompiler create();

    ICompiler create(final int numberOfWorkers);

    ICompiler create(ExecutorService executorService);
}
