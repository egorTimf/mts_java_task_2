package ru.urfu.wikisearch.api;

import ru.urfu.wikisearch.model.Article;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public final class WikipediaClient implements SearchService {
    private static final URI DEFAULT_ENDPOINT = URI.create("https://ru.wikipedia.org/w/api.php");

    private final HttpClient client;
    private final WikipediaResponseParser parser;
    private final URI endpoint;
    private final Duration requestTimeout;

    public WikipediaClient() {
        this(HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build(),
                new WikipediaResponseParser(), DEFAULT_ENDPOINT, Duration.ofSeconds(20));
    }

    public WikipediaClient(HttpClient client, WikipediaResponseParser parser,
                           URI endpoint, Duration requestTimeout) {
        this.client = client;
        this.parser = parser;
        this.endpoint = endpoint;
        this.requestTimeout = requestTimeout;
    }

    @Override
    public List<Article> search(String query) throws SearchException, InterruptedException {
        if (query == null || query.isBlank()) {
            throw new SearchException("Поисковый запрос не должен быть пустым.");
        }
        String encodedQuery = URLEncoder.encode(query.strip(), StandardCharsets.UTF_8);
        URI uri = URI.create(endpoint + "?action=query&list=search&format=json&utf8=1"
                + "&srnamespace=0&srlimit=10&srprop=snippet&srsearch=" + encodedQuery);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(requestTimeout)
                .header("Accept", "application/json")
                .header("User-Agent", "UrFU-WikipediaSearch/1.0 (Java console educational project)")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 429) {
                throw new SearchException("Слишком много запросов. Подождите и повторите поиск.");
            }
            if (response.statusCode() != 200) {
                throw new SearchException("Сервер Википедии вернул ошибку HTTP " + response.statusCode() + ".");
            }
            return parser.parse(response.body());
        } catch (HttpTimeoutException exception) {
            throw new SearchException("Время ожидания ответа истекло. Повторите поиск.", exception);
        } catch (IOException exception) {
            throw new SearchException("Не удалось связаться с Википедией. Проверьте подключение к интернету.", exception);
        }
    }
}
