package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.core.entities.Ship;
import org.jetbrains.annotations.Nullable;

@ValueData(name = "name", description = "Gets the name of the ship or npc", example = "name(ship)")
public class ShipName implements Value<String> {

    public Value<Ship> ship;

    @Override
    public @Nullable String get(Main main) {
        Ship sh = Value.get(ship, main);
        return sh != null ? sh.playerInfo.username : null;
    }

    @Override
    public String toString() {
        return "name(" + ship + ")";
    }
}
