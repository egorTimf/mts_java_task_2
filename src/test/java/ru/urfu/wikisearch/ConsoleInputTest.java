package ru.urfu.wikisearch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.urfu.wikisearch.console.ConsoleInput;
import ru.urfu.wikisearch.console.ConsoleView;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleInputTest {
    private final StringWriter output = new StringWriter();

    private ConsoleInput input(String text) {
        return new ConsoleInput(new StringReader(text), new ConsoleView(new PrintWriter(output)));
    }

    @Test
    void skipsEmptyQueriesAndTrimsSpaces() throws IOException {
        assertEquals("ЛЭТИ", input("\n   \n  ЛЭТИ  \n").readQuery());
        assertTrue(output.toString().contains("непустой"));
    }

    @Test
    void rejectsOversizedAndControlCharacterQueries() throws IOException {
        assertEquals("Java", input("a".repeat(301) + "\nabc\u001b\nJava\n").readQuery());
        assertTrue(output.toString().contains("300"));
        assertTrue(output.toString().contains("управляющие"));
    }

    @Test
    void countsUnicodeCodePoints() throws IOException {
        String query = "😀".repeat(300);
        assertEquals(query, input(query + "\n").readQuery());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", ":q\n", " ВыХоД \n", ":Q\n"})
    void stopsOnExitOrEndOfInput(String text) throws IOException {
        assertNull(input(text).readQuery());
        assertEquals(-1, input(text).readSelection(3));
    }

    @Test
    void repeatsSelectionUntilValidNumber() throws IOException {
        assertEquals(2, input("\nabc\n1.5\n-1\n4\n99999999999999999999\n 2 \n")
                .readSelection(3));
        assertTrue(output.toString().contains("целое число"));
        assertTrue(output.toString().contains("от 0 до 3"));
    }

    @Test
    void acceptsNewSearchCommand() throws IOException {
        assertEquals(0, input("0\n").readSelection(3));
    }
}
