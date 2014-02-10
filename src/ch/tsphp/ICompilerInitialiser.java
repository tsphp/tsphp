package ch.tsphp;

import ch.tsphp.common.ICompiler;

public interface ICompilerInitialiser
{

    ICompiler create();

    ICompiler create(final int numberOfWorkers);
}
