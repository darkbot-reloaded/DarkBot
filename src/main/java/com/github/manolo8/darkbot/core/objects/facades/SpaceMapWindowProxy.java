package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import eu.darkbot.api.API;

public class SpaceMapWindowProxy extends Updatable implements API.Singleton {

    public final JumpInfo jumpInfo = new JumpInfo();

    @Override
    public void update() {
        jumpInfo.update(Main.API.readLong(address + 80));
    }

    public static class JumpInfo extends Updatable.Auto {
        public int duration, mapId;

        @Override
        public void update() {
            if (address == 0) return;

            duration = Main.API.readInt(address + 32);
            mapId = Main.API.readInt(address + 36);
        }

        @Override
        public void update(long address) {
            if (this.address != 0 && address == 0) {
                duration = 0;
                mapId = 0;
            }
            super.update(address);
        }
    }
}
