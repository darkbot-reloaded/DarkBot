package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.facades.*;
import com.github.manolo8.darkbot.core.objects.swf.PairArray;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class FacadeManager implements Manager {
    private final Main main;
    private final PairArray commands         = PairArray.ofArray();
    private final PairArray proxies          = PairArray.ofArray();
    private final PairArray mediators        = PairArray.ofArray();
    private final List<Updatable> updatables = new ArrayList<>();

    public final LogMediator log                       = registerMediator("LogWindowMediator", new LogMediator());
    public final ChatProxy chat                        = registerProxy("ChatProxy", new ChatProxy());
    public final StatsProxy stats                      = registerProxy("StatsProxy", new StatsProxy());
    public final EscortProxy escort                    = registerProxy("payload_escort", new EscortProxy());
    public final BoosterProxy booster                  = registerProxy("BoosterProxy", new BoosterProxy());
    public final SettingsProxy settings                = registerProxy("SettingsWindowFUIProxy", new SettingsProxy());
    public final SlotBarsProxy slotBars                = registerProxy("ItemsControlMenuProxy", new SlotBarsProxy(settings));
    public final EternalGateProxy eternalGate          = registerProxy("eternal_gate", new EternalGateProxy());
    public final EternalBlacklightProxy blacklightGate = registerProxy("eternal_blacklight", new EternalBlacklightProxy());
    public final ChrominProxy chrominEvent             = registerProxy("chrominEvent", new ChrominProxy());

    public FacadeManager(Main main) {
        this.main = main;
    }

    public <T extends Updatable> T registerCommand(String key, T command) {
        this.commands.addLazy(key, ((Updatable) command)::update);
        updatables.add(command);
        return command;
    }

    public <T extends Updatable> T registerProxy(String key, T proxy) {
        this.proxies.addLazy(key, ((Updatable) proxy)::update);
        updatables.add(proxy);
        return proxy;
    }

    public <T extends Updatable> T registerMediator(String key, T mediator) {
        this.mediators.addLazy(key, ((Updatable) mediator)::update);
        updatables.add(mediator);
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
        // Currently commands are not used by the bot and they represent
        // a decently big cpu chunk in ticking. Leaving them out reduces tick time significantly.
        //commands.update();
        proxies.update();
        mediators.update();

        for (Updatable updatable : updatables) {
            if (updatable.address != 0) updatable.update();
        }
    }
}
