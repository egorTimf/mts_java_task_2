package ru.urfu.wikisearch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.urfu.wikisearch.api.SearchException;
import ru.urfu.wikisearch.api.WikipediaResponseParser;
import ru.urfu.wikisearch.model.Article;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WikipediaResponseParserTest {
    private final WikipediaResponseParser parser = new WikipediaResponseParser();

    @Test
    void readsArticlesAndConvertsHtml() throws SearchException {
        String json = """
                {"query":{"search":[
                  {"pageid":11099,"title":"ЛЭТИ","snippet":"<span class=\\\"searchmatch\\\">ЛЭТИ</span> &amp; наука&nbsp;&#169;"},
                  {"pageid":7000000000,"title":"Java"}
                ]}}
                """;
        List<Article> articles = parser.parse(json);
        assertEquals(2, articles.size());
        assertEquals("ЛЭТИ & наука ©", articles.get(0).snippet());
        assertEquals("https://ru.wikipedia.org/w/index.php?curid=11099", articles.get(0).uri().toString());
        assertEquals(7000000000L, articles.get(1).pageId());
        assertEquals("", articles.get(1).snippet());
    }

    @Test
    void returnsEmptyListForNoMatches() throws SearchException {
        assertTrue(parser.parse("{\"query\":{\"search\":[]}}").isEmpty());
    }

    @Test
    void reportsApiError() {
        SearchException error = assertThrows(SearchException.class,
                () -> parser.parse("{\"error\":{\"code\":\"badvalue\",\"info\":\"Invalid request\"}}"));
        assertTrue(error.getMessage().contains("отклонила"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "null", "[]", "{}", "{", "<html>Error</html>",
            "{\"query\":null}", "{\"query\":[]}", "{\"query\":{\"search\":null}}",
            "{\"query\":{\"search\":{}}}", "{\"query\":{\"search\":[null]}}",
            "{\"query\":{\"search\":[{}]}}", "{query:{search:[]}}",
            "{\"query\":{\"search\":[]}} trailing",
            "{\"query\":{\"search\":[{\"pageid\":1.5,\"title\":\"A\"}]}}",
            "{\"query\":{\"search\":[{\"pageid\":0,\"title\":\"A\"}]}}",
            "{\"query\":{\"search\":[{\"pageid\":1,\"title\":\" \"}]}}",
            "{\"query\":{\"search\":[{\"pageid\":1,\"title\":4}]}}",
            "{\"query\":{\"search\":[{\"pageid\":\"12\",\"title\":\"A\"}]}}",
            "{\"query\":{\"search\":[{\"pageid\":99999999999999999999,\"title\":\"A\"}]}}"
    })
    void reportsMalformedResponses(String json) {
        assertThrows(SearchException.class, () -> parser.parse(json));
    }
}
