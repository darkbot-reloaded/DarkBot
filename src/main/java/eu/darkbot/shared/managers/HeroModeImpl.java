package eu.darkbot.shared.managers;

import eu.darkbot.api.items.SelectableItem;
import eu.darkbot.api.managers.HeroAPI;

public class HeroModeImpl implements HeroAPI.Mode {

    private final HeroAPI.Configuration configuration;
    private final SelectableItem.Formation formation;

    public HeroModeImpl(HeroAPI.Configuration configuration, SelectableItem.Formation formation) {
        this.configuration = configuration;
        this.formation = formation;
    }

    @Override
    public HeroAPI.Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public SelectableItem.Formation getFormation() {
        return formation;
    }
}
