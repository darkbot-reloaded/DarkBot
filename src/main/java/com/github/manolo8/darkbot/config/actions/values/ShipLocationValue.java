package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.utils.Location;
import org.jetbrains.annotations.Nullable;

@ValueData(name = "ship-loc", description = "Gets the current location of a ship", example = "ship-loc(a)")
public class ShipLocationValue implements Value<Location> {

    public Value<Ship> ship;

    @Override
    public String toString() {
        return "ship-loc(" + ship + ")";
    }

    @Override
    public @Nullable Location get(Main main) {
        Ship sh = Value.get(ship, main);
        return sh != null ? sh.locationInfo.now : null;
    }
}
