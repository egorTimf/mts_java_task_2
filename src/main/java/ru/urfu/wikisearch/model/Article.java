package ru.urfu.wikisearch.model;

import java.net.URI;
import java.util.Objects;

public record Article(long pageId, String title, String snippet) {
    public Article {
        if (pageId <= 0) {
            throw new IllegalArgumentException("Идентификатор статьи должен быть положительным.");
        }
        Objects.requireNonNull(title);
        Objects.requireNonNull(snippet);
        if (title.isBlank()) {
            throw new IllegalArgumentException("Название статьи не должно быть пустым.");
        }
    }

    public URI uri() {
        return URI.create("https://ru.wikipedia.org/w/index.php?curid=" + pageId);
    }
}
