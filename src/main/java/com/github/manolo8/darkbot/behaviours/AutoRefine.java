package com.github.manolo8.darkbot.behaviours;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.manager.GuiManager;
import eu.darkbot.api.extensions.Behavior;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.managers.OreAPI;
import eu.darkbot.util.Timer;

import java.util.Arrays;
import java.util.Comparator;

import static eu.darkbot.api.managers.OreAPI.*;

@Feature(name = "Auto refiner", description = "Automatically refine raw materials")
public class AutoRefine implements Behavior {

    private final Main main;
    private final OreAPI ores;
    private final GuiManager guiManager;
    private final IDarkBotAPI darkbotApi;

    private final Timer timer = Timer.get(250);

    public AutoRefine(Main main,
                      OreAPI ores,
                      GuiManager guiManager,
                      IDarkBotAPI darkbotApi) {
        this.main = main;
        this.ores = ores;
        this.guiManager = guiManager;
        this.darkbotApi = darkbotApi;
    }

    @Override
    public void onTickBehavior() {
        if (!main.config.MISCELLANEOUS.AUTO_REFINE ||
                !darkbotApi.hasCapability(Capability.DIRECT_REFINE) ||
                guiManager.getAddress() == 0) return;

        Arrays.stream(Ore.values())
                .max(Comparator.comparingInt(this::maxRefine))
                .ifPresent(ore -> {
                    int maxRefine = maxRefine(ore);
                    if (maxRefine <= 0 || !timer.tryActivate()) return;
                    darkbotApi.refine(darkbotApi.readMemoryLong(guiManager.getAddress() + 0x78), ore, maxRefine);
                });
    }

    private int maxRefine(OreAPI.Ore ore) {
        switch (ore) {
            case PROMETID:
                return Math.min(ores.getAmount(Ore.PROMETIUM) / 20, ores.getAmount(Ore.ENDURIUM) / 10);
            case DURANIUM:
                return Math.min(ores.getAmount(Ore.TERBIUM) / 20, ores.getAmount(Ore.ENDURIUM) / 10);
            case PROMERIUM:
                return Math.min(
                        Math.min(ores.getAmount(Ore.PROMETID) / 10, ores.getAmount(Ore.DURANIUM) / 10),
                        ores.getAmount(Ore.XENOMIT));
            default:
                return 0;
        }
    }

}
