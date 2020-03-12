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

public class FacadeManager implements Manager {
    private final Main main;
    private final EntryArray commands = new EntryArray();
    private final EntryArray proxies = new EntryArray();
    private final EntryArray mediators = new EntryArray();

    private final List<Updatable> updatables = new ArrayList<>();

    public final LogMediator log = registerMediator("LogWindowMediator", new LogMediator());


    public FacadeManager(Main main) {
        this.main = main;
    }

    public <T extends Updatable> T registerCommand(String key, T mediator) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        Updatable fix = mediator; // Workaround for a java compiler assertion bug having issues with types
        this.commands.addLazy(key, fix::update);
        updatables.add(fix);
        return mediator;
    }

    public <T extends Updatable> T registerProxy(String key, T mediator) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        Updatable fix = mediator; // Workaround for a java compiler assertion bug having issues with types
        this.proxies.addLazy(key, fix::update);
        updatables.add(fix);
        return mediator;
    }

    public <T extends Updatable> T registerMediator(String key, T mediator) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        Updatable fix = mediator; // Workaround for a java compiler assertion bug having issues with types
        this.mediators.addLazy(key, fix::update);
        updatables.add(fix);
        return mediator;
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.mainAddress.add(mainAddr -> {
            long facade = API.readMemoryLong(mainAddr + 544);

            commands.update(API.readMemoryLong(facade, 0x28, 0x20));
            proxies.update(API.readMemoryLong(facade, 0x38, 0x30));
            mediators.update(API.readMemoryLong(facade, 0x40, 0x38));
        });
    }

    public void tick() {
        commands.update();
        proxies.update();
        mediators.update();

        updatables.forEach(Updatable::update);
    }

}
