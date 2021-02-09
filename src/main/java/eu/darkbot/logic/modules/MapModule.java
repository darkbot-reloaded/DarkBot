package eu.darkbot.logic.modules;

import com.github.manolo8.darkbot.utils.I18n;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.entities.utils.Map;
import eu.darkbot.api.extensions.Installable;
import eu.darkbot.logic.utils.MapTraveler;

public class MapModule extends TemporalModule implements Installable {

    protected MapTraveler traveler;

    public MapModule(PluginAPI api) {
        super(api);
        this.traveler = api.requireInstance(MapTraveler.class);
    }

    @Override
    public void install(PluginAPI pluginAPI) {
        traveler.install(pluginAPI);
    }

    @Override
    public void uninstall() {
        traveler.uninstall();
    }

    public void setTarget(Map target) {
        this.traveler.setTarget(target);
    }

    @Override
    public String getStatus() {
        return  traveler.current != null ?
                I18n.get("module.map_travel.status.has_next", traveler.target.getName(),
                        traveler.current.getTargetMap().map(Map::getName).orElse("unknown?")) :
                I18n.get("module.map_travel.status.no_next", traveler.target.getName());
    }

    @Override
    public void onTickModule() {
        if (!traveler.isDone()) traveler.tick();
        if (traveler.isDone()) goBack();
    }

}
