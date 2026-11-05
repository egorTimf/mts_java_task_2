package ru.urfu.wikisearch.api;

import ru.urfu.wikisearch.model.Article;

import java.util.List;

public interface SearchService {
    List<Article> search(String query) throws SearchException, InterruptedException;
}
