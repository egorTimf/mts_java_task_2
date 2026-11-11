package ru.urfu.wikisearch;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.urfu.wikisearch.api.SearchException;
import ru.urfu.wikisearch.api.WikipediaClient;
import ru.urfu.wikisearch.api.WikipediaResponseParser;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WikipediaClientTest {
    private HttpServer server;
    private WikipediaClient client;
    private final AtomicReference<String> requestQuery = new AtomicReference<>();
    private final AtomicReference<String> userAgent = new AtomicReference<>();
    private int status = 200;
    private String body = "{\"query\":{\"search\":[{\"pageid\":1,\"title\":\"ЛЭТИ\"}]}}";

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api.php", exchange -> {
            requestQuery.set(exchange.getRequestURI().getRawQuery());
            userAgent.set(exchange.getRequestHeaders().getFirst("User-Agent"));
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (var output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
        server.start();
        client = new WikipediaClient(HttpClient.newHttpClient(), new WikipediaResponseParser(),
                endpoint(), Duration.ofSeconds(2));
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private URI endpoint() {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api.php");
    }

    @Test
    void sendsEncodedUnicodeAndSpecialCharacters() throws Exception {
        String query = "ЛЭТИ C++ & #? / 😀";
        assertEquals("ЛЭТИ", client.search(query).get(0).title());
        String raw = requestQuery.get();
        String value = raw.substring(raw.indexOf("srsearch=") + "srsearch=".length());
        assertEquals(query, URLDecoder.decode(value, StandardCharsets.UTF_8));
        assertTrue(raw.contains("srlimit=10"));
        assertTrue(raw.contains("srnamespace=0"));
        assertTrue(userAgent.get().startsWith("UrFU-WikipediaSearch/"));
    }

    @ParameterizedTest
    @ValueSource(ints = {403, 429, 500, 503})
    void reportsHttpErrors(int code) {
        status = code;
        SearchException exception = assertThrows(SearchException.class, () -> client.search("Java"));
        assertTrue(exception.getMessage().contains(code == 429 ? "Слишком много" : "HTTP " + code));
    }

    @Test
    void reportsInvalidServerResponse() {
        body = "<html>Unavailable</html>";
        assertThrows(SearchException.class, () -> client.search("Java"));
    }

    @Test
    void reportsConnectionFailure() {
        server.stop(0);
        SearchException exception = assertThrows(SearchException.class, () -> client.search("Java"));
        assertTrue(exception.getMessage().contains("подключение"));
    }

    @Test
    void reportsTimeout() {
        server.removeContext("/api.php");
        server.createContext("/api.php", exchange -> {
        });
        WikipediaClient shortTimeoutClient = new WikipediaClient(HttpClient.newHttpClient(),
                new WikipediaResponseParser(), endpoint(), Duration.ofMillis(100));
        SearchException exception = assertThrows(SearchException.class,
                () -> shortTimeoutClient.search("Java"));
        assertTrue(exception.getMessage().contains("ожидания"));
    }

    @Test
    void rejectsEmptyQueries() {
        assertThrows(SearchException.class, () -> client.search("   "));
        assertThrows(SearchException.class, () -> client.search(null));
    }
}
