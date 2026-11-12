package ru.urfu.wikisearch;

import org.junit.jupiter.api.Test;
import ru.urfu.wikisearch.api.SearchException;
import ru.urfu.wikisearch.api.SearchService;
import ru.urfu.wikisearch.browser.BrowserOpener;
import ru.urfu.wikisearch.console.ConsoleInput;
import ru.urfu.wikisearch.console.ConsoleView;
import ru.urfu.wikisearch.model.Article;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WikipediaApplicationTest {
    private final List<Article> articles = List.of(new Article(11099, "ЛЭТИ", "Университет"));

    private String run(String commands, SearchService service, BrowserOpener browser) {
        StringWriter output = new StringWriter();
        ConsoleView view = new ConsoleView(new PrintWriter(output));
        ConsoleInput input = new ConsoleInput(new StringReader(commands), view);
        new WikipediaApplication(input, view, service, browser).run();
        return output.toString();
    }

    @Test
    void opensSelectedArticleAndAllowsNewSearch() {
        List<String> queries = new ArrayList<>();
        List<URI> opened = new ArrayList<>();
        String output = run("ЛЭТИ\n1\n0\nJava\n1\n:q\n", query -> {
            queries.add(query);
            return articles;
        }, opened::add);
        assertEquals(List.of("ЛЭТИ", "Java"), queries);
        assertEquals(List.of(articles.get(0).uri(), articles.get(0).uri()), opened);
        assertTrue(output.contains("Программа завершена"));
    }

    @Test
    void recoversAfterSearchFailureAndNoMatches() {
        List<String> queries = new ArrayList<>();
        String output = run("ошибка\nпусто\nЛЭТИ\n:q\n", query -> {
            queries.add(query);
            if (query.equals("ошибка")) {
                throw new SearchException("Ошибка сети");
            }
            return query.equals("пусто") ? List.of() : articles;
        }, uri -> {
        });
        assertEquals(List.of("ошибка", "пусто", "ЛЭТИ"), queries);
        assertTrue(output.contains("Ошибка сети"));
        assertTrue(output.contains("Ничего не найдено"));
        assertTrue(output.contains("1. ЛЭТИ"));
    }

    @Test
    void printsLinkWhenBrowserCannotBeOpened() {
        String output = run("ЛЭТИ\n1\n:q\n", query -> articles, uri -> {
            throw new IOException("Browser unavailable");
        });
        assertTrue(output.contains("Откройте ссылку вручную"));
        assertTrue(output.contains("https://ru.wikipedia.org/w/index.php?curid=11099"));
        assertTrue(output.contains("Программа завершена"));
    }

    @Test
    void stopsOnEndOfInputDuringSelection() {
        String output = run("ЛЭТИ\n", query -> articles, uri -> {
        });
        assertTrue(output.contains("Программа завершена"));
    }

    @Test
    void preservesInterruptionFlag() {
        try {
            String output = run("Java\n", query -> {
                throw new InterruptedException();
            }, uri -> {
            });
            assertTrue(Thread.currentThread().isInterrupted());
            assertTrue(output.contains("Поиск прерван"));
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void handlesInputFailure() {
        Reader brokenReader = new Reader() {
            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                throw new IOException("Input unavailable");
            }

            @Override
            public void close() {
            }
        };
        StringWriter output = new StringWriter();
        ConsoleView view = new ConsoleView(new PrintWriter(output));
        new WikipediaApplication(new ConsoleInput(brokenReader, view), view,
                query -> articles, uri -> {
                }).run();
        assertTrue(output.toString().contains("Не удалось прочитать"));
    }
}
