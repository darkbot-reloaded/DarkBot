package eu.darkbot.logic;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.PluginAPI;

public class SafetyFinder extends com.github.manolo8.darkbot.modules.utils.SafetyFinder {

    public SafetyFinder(PluginAPI api) {
        super(api.requireAPI(Main.class));
    }
}
