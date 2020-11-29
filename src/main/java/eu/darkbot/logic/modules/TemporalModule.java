package eu.darkbot.logic.modules;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.plugin.Module;

abstract class TemporalModule implements Module {

    protected final PluginAPI api;
    protected Module back;

    protected TemporalModule(PluginAPI api) {
        this.api = api;
        this.back = api.getModule();

        if (back instanceof TemporalModule)
            this.back = ((TemporalModule) this.back).back;
    }

    protected void goBack() {
        this.api.setModule(back);
        this.back = null;
    }
}
