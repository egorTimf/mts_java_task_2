package ru.urfu.wikisearch.console;

import ru.urfu.wikisearch.model.Article;

import java.io.PrintWriter;
import java.util.List;

public final class ConsoleView {
    private final PrintWriter writer;

    public ConsoleView(PrintWriter writer) {
        this.writer = writer;
    }

    public void showWelcome() {
        showMessage("Поиск по русской Википедии");
        showMessage("Для завершения введите :q или выход.");
    }

    public void showMessage(String message) {
        writer.println(message);
        writer.flush();
    }

    public void showPrompt(String prompt) {
        writer.print(prompt);
        writer.flush();
    }

    public void showResults(List<Article> articles) {
        showMessage("Найдено статей: " + articles.size());
        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            showMessage((i + 1) + ". " + article.title());
            if (!article.snippet().isBlank()) {
                showMessage("   " + article.snippet());
            }
        }
    }
}
