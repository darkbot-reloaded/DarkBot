package eu.darkbot.config;

import org.jetbrains.annotations.Nullable;

public class ShipConfig {

    protected int configuration;
    protected Character formation;

    public ShipConfig() {
    }

    public ShipConfig(int configuration, Character formation) {
        this.configuration = configuration;
        this.formation = formation;
    }

    public int getConfiguration() {
        return configuration;
    }

    public void setConfiguration(int configuration) {
        this.configuration = configuration;
    }

    @Nullable
    public Character getFormation() {
        return formation;
    }

    public void setFormation(Character formation) {
        this.formation = formation;
    }
}
