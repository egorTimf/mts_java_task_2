package ru.urfu.wikisearch.browser;

import java.io.IOException;
import java.net.URI;

public interface BrowserOpener {
    void open(URI uri) throws IOException;
}
