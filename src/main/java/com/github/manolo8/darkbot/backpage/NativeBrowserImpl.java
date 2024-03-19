package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.extensions.util.Version;
import com.github.manolo8.darkbot.gui.titlebar.BackpageTask;
import com.github.manolo8.darkbot.utils.I18n;
import eu.darkbot.api.managers.NativeBrowserAPI;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class NativeBrowserImpl implements NativeBrowserAPI {
    private static final Version OPEN_LINK_MIN_VERSION = new Version("1.3.0");

    private final BackpageManager backpageManager;

    public NativeBrowserImpl(BackpageManager backpageManager) {
        this.backpageManager = backpageManager;
    }

    @Override
    public boolean isSupported() {
        return BackpageTask.isSupported(OPEN_LINK_MIN_VERSION);
    }

    @Override
    public @Nullable Process openLink(String link) {
        if (isSupported()) {
            try {
                return BackpageTask.createBrowser("--url", link);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public @Nullable Process openGameLink(String path) {
        if (isSupported() && backpageManager.isInstanceValid()) {
            try {
                return BackpageTask.createBrowser("--sid " + backpageManager.getSid(),
                        "--url " + backpageManager.getInstanceURI().toString(), // for sid
                        "--fullurl " + backpageManager.getInstanceURI().toString() + path,
                        "--lang " + I18n.getLocale().getLanguage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
