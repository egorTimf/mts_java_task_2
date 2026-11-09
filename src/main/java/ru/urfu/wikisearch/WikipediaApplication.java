package ru.urfu.wikisearch;

import ru.urfu.wikisearch.api.SearchException;
import ru.urfu.wikisearch.api.SearchService;
import ru.urfu.wikisearch.browser.BrowserOpener;
import ru.urfu.wikisearch.console.ConsoleInput;
import ru.urfu.wikisearch.console.ConsoleView;
import ru.urfu.wikisearch.model.Article;

import java.io.IOException;
import java.util.List;

public final class WikipediaApplication {
    private final ConsoleInput input;
    private final ConsoleView view;
    private final SearchService searchService;
    private final BrowserOpener browser;

    public WikipediaApplication(ConsoleInput input, ConsoleView view,
                                SearchService searchService, BrowserOpener browser) {
        this.input = input;
        this.view = view;
        this.searchService = searchService;
        this.browser = browser;
    }

    public void run() {
        view.showWelcome();
        try {
            while (true) {
                String query = input.readQuery();
                if (query == null) {
                    break;
                }
                try {
                    view.showMessage("Выполняется поиск...");
                    List<Article> articles = searchService.search(query);
                    if (articles.isEmpty()) {
                        view.showMessage("Ничего не найдено. Попробуйте другой запрос.");
                        continue;
                    }
                    view.showResults(articles);
                    if (!selectArticles(articles)) {
                        break;
                    }
                } catch (SearchException exception) {
                    view.showMessage(exception.getMessage());
                }
            }
        } catch (IOException exception) {
            view.showMessage("Не удалось прочитать данные из консоли.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            view.showMessage("Поиск прерван.");
        }
        view.showMessage("Программа завершена.");
    }

    private boolean selectArticles(List<Article> articles) throws IOException {
        while (true) {
            int selection = input.readSelection(articles.size());
            if (selection <= 0) {
                return selection == 0;
            }
            Article article = articles.get(selection - 1);
            try {
                browser.open(article.uri());
                view.showMessage("Запрос на открытие статьи передан браузеру: " + article.title());
            } catch (IOException exception) {
                view.showMessage("Не удалось открыть браузер. Откройте ссылку вручную:");
            }
            view.showMessage(article.uri().toString());
        }
    }
}
