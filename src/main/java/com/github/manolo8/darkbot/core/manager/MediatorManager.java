package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.mediators.LogMediator;
import com.github.manolo8.darkbot.core.objects.swf.EntryArray;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class MediatorManager implements Manager {
    private final Main main;
    private final EntryArray arrayEntry = new EntryArray();

    private final List<Updatable> mediators = new ArrayList<>();

    public final LogMediator log = register("LogWindowMediator", new LogMediator());


    public MediatorManager(Main main) {
        this.main = main;
    }

    private <T extends Updatable> T register(String key, T mediator) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        Updatable fix = mediator; // Workaround for a java compiler assertion bug having issues with types
        this.arrayEntry.addLazy(key, fix::update);
        mediators.add(fix);
        return mediator;
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.mainAddress.add(mainAddr ->
            arrayEntry.update(API.readMemoryLong(API.readMemoryLong(API.readMemoryLong(mainAddr + 544) + 0x40) + 0x38)));
    }

    public void tick() {
        arrayEntry.update();

        mediators.forEach(Updatable::update);
    }

}
