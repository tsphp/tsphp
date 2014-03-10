/*
 * This file is part of the TSPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp.console;

import ch.tsphp.HardCodedCompilerInitialiser;

public final class Main
{

    private Main() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        IConsoleReader consoleReader = new ConsoleReader(new HardCodedCompilerInitialiser().create());
        consoleReader.readArguments(args);
    }
}
