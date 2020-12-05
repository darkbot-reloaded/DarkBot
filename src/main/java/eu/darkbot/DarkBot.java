package eu.darkbot;

import com.formdev.flatlaf.FlatLaf;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.API;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.managers.BotAPI;
import eu.darkbot.api.plugin.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DarkBot extends Main implements PluginAPI, BotAPI {

    private final Map<Class<? extends API>, API> apis = new HashMap<>();

    private Module module;

    public DarkBot(StartupParams params) {
        super(params);
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends eu.darkbot.api.API> T getAPI(@NotNull Class<T> clazz) {
        return (T) apis.get(clazz);
    }

    @Override
    @NotNull
    public <T extends eu.darkbot.api.API> T requireAPI(@NotNull Class<T> clazz) throws UnsupportedOperationException {
        T api = getAPI(clazz);
        if (api == null)
            throw new UnsupportedOperationException("API " + clazz.getName() + " not supported!");

        return api;
    }

    @Override
    public Module getModule() {
        return module;
    }

    @Override
    // TODO: 29.11.2020
    public Module setModule(@NotNull Module module) {
        return (this.module = module);
    }

    @Override
    public void setTheme(FlatLaf theme) {

    }

    @Override
    public double getTickTime() {
        return avgTick;
    }
}
