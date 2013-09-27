package ch.tutteli.tsphp;

import ch.tutteli.tsphp.common.ICompiler;

public interface ICompilerInitialiser
{

    ICompiler create();

    ICompiler create(final int numberOfWorkers);
}
