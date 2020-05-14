package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.modules.utils.MapTraveler;
import com.github.manolo8.darkbot.utils.I18n;

public class MapModule extends TemporalModule {

    private MapTraveler traveler;

    @Override
    public void install(Main main) {
        super.install(main);
        if (traveler == null) this.traveler = new MapTraveler(main);
    }

    public void uninstall() {
        this.traveler.uninstall();
        this.traveler = null;
    }

    @Override
    public String status() {
        return traveler.current != null ?
                I18n.get("module.map_travel.status.has_next", traveler.target.name, traveler.current.target.name) :
                I18n.get("module.map_travel.status.no_next", traveler.target.name);
    }

    @Override
    public boolean canRefresh() {
        return false;
    }

    public void setTarget(Map target) {
        traveler.setTarget(target);
    }

    @Override
    public void tick() {
        if (!traveler.isDone()) traveler.tick();
        if (traveler.isDone()) goBack();
    }

}
