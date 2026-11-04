package ru.urfu.wikisearch.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public final class ConsoleInput {
    private static final int MAX_QUERY_LENGTH = 300;

    private final BufferedReader reader;
    private final ConsoleView view;

    public ConsoleInput(Reader reader, ConsoleView view) {
        this.reader = new BufferedReader(reader);
        this.view = view;
    }

    public String readQuery() throws IOException {
        while (true) {
            view.showPrompt("\nПоисковый запрос: ");
            String line = reader.readLine();
            if (isExit(line)) {
                return null;
            }
            String query = line.strip();
            if (query.isEmpty()) {
                view.showMessage("Введите непустой запрос.");
            } else if (query.codePointCount(0, query.length()) > MAX_QUERY_LENGTH) {
                view.showMessage("Запрос должен содержать не более 300 символов.");
            } else if (query.codePoints().anyMatch(Character::isISOControl)) {
                view.showMessage("Запрос не должен содержать управляющие символы.");
            } else {
                return query;
            }
        }
    }

    public int readSelection(int resultCount) throws IOException {
        while (true) {
            view.showPrompt("Номер статьи (1–" + resultCount + "), 0 — новый поиск: ");
            String line = reader.readLine();
            if (isExit(line)) {
                return -1;
            }
            try {
                int number = Integer.parseInt(line.strip());
                if (number >= 0 && number <= resultCount) {
                    return number;
                }
            } catch (NumberFormatException exception) {
                view.showMessage("Нужно ввести целое число.");
                continue;
            }
            view.showMessage("Введите число от 0 до " + resultCount + ".");
        }
    }

    private boolean isExit(String line) {
        return line == null || line.strip().equalsIgnoreCase(":q")
                || line.strip().equalsIgnoreCase("выход");
    }
}
