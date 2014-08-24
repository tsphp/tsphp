/*
 * This file is part of the TSPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp.test.unit;

import ch.tsphp.common.ICompiler;
import ch.tsphp.console.ConsoleReader;
import ch.tsphp.console.IConsoleReader;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ConsoleReaderTest
{

    @Test
    public void readArguments_noArgumentsProvided_NoInteractionWithCompiler() {
        ICompiler compiler = mock(ICompiler.class);

        IConsoleReader consoleReader = createConsoleReader(compiler);
        consoleReader.readArguments(new String[]{});

        verifyNoMoreInteractions(compiler);
    }

    @Test
    public void readArguments_OneArgument_UseArgumentAsFilePathAndAddToCompilationUnits() throws IOException {
        ICompiler compiler = mock(ICompiler.class);
        String file = "file";

        IConsoleReader consoleReader = createConsoleReader(compiler);
        consoleReader.readArguments(new String[]{file});

        verify(compiler).addFile(file);
    }

    protected IConsoleReader createConsoleReader(ICompiler compiler) {
        return new ConsoleReader(compiler);
    }
}
