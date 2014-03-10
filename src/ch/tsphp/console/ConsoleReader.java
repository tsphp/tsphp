/*
 * This file is part of the TSPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp.console;

import ch.tsphp.common.ICompiler;
import java.io.IOException;

public class ConsoleReader implements IConsoleReader
{

    private final ICompiler compilerController;

    public ConsoleReader(ICompiler controller) {
        compilerController = controller;
    }

    @Override
    public void readArguments(String[] args) {
        if (args.length > 0) {
            addFile(args[0]);
            compilerController.compile();
        }
    }

    @Override
    public void addFile(String path) {
        try {
            compilerController.addFile(path);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void addDirectory(String path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
