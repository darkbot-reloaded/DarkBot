package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.core.entities.Ship;

@ValueData(name = "hero", description = "Gets the bot hero, aka: your ship", example = "hero()")
public class HeroValue implements Value<Ship> {

    @Override
    public Ship get(Main main) {
        return main.hero;
    }

    @Override
    public String toString() {
        return "hero()";
    }
}
