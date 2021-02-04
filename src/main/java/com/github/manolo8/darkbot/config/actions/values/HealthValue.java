package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.objects.Health;

import javax.swing.*;

@ValueData("health")
public class HealthValue implements Value<Health> {

    public Value<Ship> ship;

    @Override
    public Health getValue(Main main) {
        if (ship == null) return null;
        Ship shValue = ship.getValue(main);
        if (shValue == null) return null;
        return shValue.health;
    }

    @Override
    public String toString() {
        return "health(" + ship + ")";
    }
}
