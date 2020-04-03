package com.github.manolo8.darkbot.behaviours;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.Behaviour;
import com.github.manolo8.darkbot.core.manager.EffectManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.modules.DisconnectModule;
import com.github.manolo8.darkbot.utils.I18n;

import java.util.List;

@Feature(name = "Anti push", description = "Turns off the bot if an enemy uses draw fire")
public class AntiPush implements Behaviour {

    private MapManager mapManager;
    private List<Ship> ships;
    private Main main;

    @Override
    public void install(Main main) {
        this.main = main;
        this.mapManager = main.mapManager;
        this.ships = main.mapManager.entities.ships;
    }

    @Override
    public void tick() {
        for (Ship ship : ships) {
            if (ship.playerInfo.isEnemy() && ship.hasEffect(EffectManager.Effect.DRAW_FIRE) && mapManager.isTarget(ship)) {
                System.out.println("Paused bot, enemy used draw fire");
                main.setModule(new DisconnectModule(null, I18n.get("module.disconnect.reason.draw_fire")));
            }
        }
    }

}
