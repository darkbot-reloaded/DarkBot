package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.modules.utils.MapTraveler;

public class MapModule extends TemporalModule {

    private MapTraveler traveler;

    @Override
    public void install(Main main) {
        super.install(main);
        this.traveler = new MapTraveler(main);
    }

    public void uninstall() {
        this.traveler.uninstall();
    }

    @Override
    public String status() {
        return "Traveling to " + traveler.target.name + (traveler.current != null ? ", next map: " + traveler.current.target.name : "");
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
