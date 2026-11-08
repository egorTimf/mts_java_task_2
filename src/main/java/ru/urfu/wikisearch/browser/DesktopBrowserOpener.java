package ru.urfu.wikisearch.browser;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

public final class DesktopBrowserOpener implements BrowserOpener {
    @Override
    public void open(URI uri) throws IOException {
        try {
            if (!Desktop.isDesktopSupported()) {
                throw new IOException("Открытие браузера не поддерживается в этой среде.");
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                throw new IOException("Браузер по умолчанию недоступен.");
            }
            desktop.browse(uri);
        } catch (UnsupportedOperationException | SecurityException exception) {
            throw new IOException("Не удалось запустить браузер.", exception);
        }
    }
}
