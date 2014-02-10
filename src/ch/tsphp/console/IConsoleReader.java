package ch.tsphp.console;

interface IConsoleReader
{

    void readArguments(String[] args);

    void addFile(String path);

    void addDirectory(String path);
}
