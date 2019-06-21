package com.github.manolo8.darkbot.core.itf;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import com.google.gson.JsonObject;

public interface CustomModule<C> extends Module {

    /**
     * @return Display name of the module in the config tree.
     */
    default String name() {
        return getClass().getSimpleName();
    }

    /**
     * @return Name of the author or authors.
     */
    default String author() {
        return "no_author";
    }

    /**
     * @return String that uniquely identifies this module. Used to store configurations under this key.
     */
    default String id() {
        return name() + "_" + author();
    }

    /**
     * @return The class used for configuration. Null if the module doesn't use configuration.
     */
    default Class<C> configuration() {
        return null;
    }

    default void install(Main main) {
        C config = null;
        Class<C> configClass = configuration();
        if (configClass == null) {
            Object storedConfig = main.config.CUSTOM_CONFIGS.get(name());
            if (configuration().isInstance(storedConfig)) config = configuration().cast(storedConfig);
            else if (storedConfig instanceof JsonObject) Main.GSON.fromJson((JsonObject) storedConfig, configuration());
            else config = ReflectionUtils.createInstance(configuration());
            main.config.CUSTOM_CONFIGS.put(name(), config);
        }
        install(main, config);
    }

    void install(Main main, C config);

}
