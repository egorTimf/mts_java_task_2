package ru.urfu.wikisearch.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import ru.urfu.wikisearch.model.Article;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class WikipediaResponseParser {
    private final Gson gson = new GsonBuilder().setStrictness(Strictness.STRICT).create();
    private final HtmlTextConverter htmlConverter = new HtmlTextConverter();

    public List<Article> parse(String json) throws SearchException {
        try {
            JsonObject root = gson.fromJson(json, JsonObject.class);
            if (root == null) {
                throw new IllegalStateException();
            }
            if (root.has("error")) {
                throw new SearchException("Википедия отклонила запрос. Измените запрос или повторите позже.");
            }
            JsonObject query = root.getAsJsonObject("query");
            if (query == null || !query.has("search") || !query.get("search").isJsonArray()) {
                throw new IllegalStateException();
            }
            JsonArray search = query.getAsJsonArray("search");
            List<Article> articles = new ArrayList<>();
            for (JsonElement element : search) {
                JsonObject item = element.getAsJsonObject();
                JsonElement id = item.get("pageid");
                if (id == null || !id.isJsonPrimitive() || !id.getAsJsonPrimitive().isNumber()) {
                    throw new IllegalStateException();
                }
                long pageId = id.getAsBigDecimal().longValueExact();
                String title = readString(item, "title");
                String snippet = item.has("snippet") ? readString(item, "snippet") : "";
                articles.add(new Article(pageId, title, htmlConverter.convert(snippet)));
            }
            return List.copyOf(articles);
        } catch (JsonParseException | IllegalStateException | IllegalArgumentException | ClassCastException
                 | ArithmeticException | IOException exception) {
            throw new SearchException("Не удалось разобрать ответ Википедии. Повторите поиск позже.", exception);
        }
    }

    private String readString(JsonObject object, String name) {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalStateException();
        }
        return value.getAsString();
    }
}
