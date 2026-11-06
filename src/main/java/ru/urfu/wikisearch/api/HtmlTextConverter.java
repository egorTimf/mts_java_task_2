package ru.urfu.wikisearch.api;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.IOException;
import java.io.StringReader;

public final class HtmlTextConverter {
    public String convert(String html) throws IOException {
        StringBuilder result = new StringBuilder();
        new ParserDelegator().parse(new StringReader(html), new HTMLEditorKit.ParserCallback() {
            @Override
            public void handleText(char[] data, int position) {
                result.append(data);
            }
        }, true);
        return result.toString()
                .replace('\u00a0', ' ')
                .replaceAll("[\\p{Cntrl}]", " ")
                .replaceAll("\\s+", " ")
                .strip();
    }
}
