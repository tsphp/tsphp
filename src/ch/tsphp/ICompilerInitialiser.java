/*
 * This file is part of the TSPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp;

import ch.tsphp.common.ICompiler;

import java.util.concurrent.ExecutorService;

public interface ICompilerInitialiser
{

    ICompiler create();

    ICompiler create(final int numberOfWorkers);

    ICompiler create(ExecutorService executorService);
}
