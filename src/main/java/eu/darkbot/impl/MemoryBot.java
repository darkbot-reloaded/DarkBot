package eu.darkbot.impl;

import com.formdev.flatlaf.FlatLaf;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.extensions.Module;
import eu.darkbot.api.managers.BotAPI;
import org.jetbrains.annotations.NotNull;

public class MemoryBot extends Main implements BotAPI {

    public MemoryBot(StartupParams params) {
        super(params);
    }

    @Override
    public double getTickTime() {
        return avgTick;
    }

    @Override
    public Module getModule() {
        return null;
    }

    @Override
    public <A extends Module> A setModule(A module) {
        return null;
    }

    @Override
    public void setTheme(FlatLaf theme) {

    }
}
