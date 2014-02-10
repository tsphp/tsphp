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
