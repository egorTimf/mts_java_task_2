package ru.urfu.wikisearch;

import ru.urfu.wikisearch.api.WikipediaClient;
import ru.urfu.wikisearch.browser.DesktopBrowserOpener;
import ru.urfu.wikisearch.console.ConsoleInput;
import ru.urfu.wikisearch.console.ConsoleView;

import java.io.Console;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Console console = System.console();
        Reader reader = console == null
                ? new InputStreamReader(System.in, StandardCharsets.UTF_8) : console.reader();
        PrintWriter writer = console == null
                ? new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true)
                : console.writer();
        ConsoleView view = new ConsoleView(writer);
        ConsoleInput input = new ConsoleInput(reader, view);
        new WikipediaApplication(input, view, new WikipediaClient(), new DesktopBrowserOpener()).run();
    }
}
