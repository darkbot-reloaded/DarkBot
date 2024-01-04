package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.extensions.util.Version;
import com.github.manolo8.darkbot.gui.titlebar.BackpageTask;
import eu.darkbot.api.managers.NativeBrowserAPI;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class NativeBrowserImpl implements NativeBrowserAPI {

    private final BackpageManager backpageManager;

    public NativeBrowserImpl(BackpageManager backpageManager) {
        this.backpageManager = backpageManager;
    }

    @Override
    public boolean isSupported() {
        return BackpageTask.isSupported(new Version("1.3.0"));
    }

    @Override
    public @Nullable Process openLink(String link) {
        if (isSupported() && backpageManager.isInstanceValid()) {
            try {
                return BackpageTask.createBrowser("--sid " + backpageManager.getSid(),
                        "--url " + backpageManager.getInstanceURI().toString(), // for sid
                        "--fullurl " + link);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public @Nullable Process openGameLink(String path) {
        return openLink(backpageManager.getInstanceURI().toString() + path);
    }
}
