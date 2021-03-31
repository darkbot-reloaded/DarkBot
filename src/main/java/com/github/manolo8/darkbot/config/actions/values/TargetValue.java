package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.core.entities.Ship;

@ValueData(name = "target", description = "Gets the ship or npc that you're shooting at", example = "target()")
public class TargetValue implements Value<Ship> {

    @Override
    public Ship get(Main main) {
        return main.hero.target;
    }

    @Override
    public String toString() {
        return "target()";
    }
}
