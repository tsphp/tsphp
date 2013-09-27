package ch.tutteli.tsphp.console;

import ch.tutteli.tsphp.common.ICompiler;
import java.io.IOException;

public class ConsoleReader implements IConsoleReader
{

    private ICompiler compilerController;

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
