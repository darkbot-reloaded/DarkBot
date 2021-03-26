package com.github.manolo8.darkbot.extensions;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.github.manolo8.darkbot.backpage.FlashResManager;
import com.github.manolo8.darkbot.core.manager.*;
import com.github.manolo8.darkbot.core.objects.facades.*;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.EntityList;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import eu.darkbot.api.API;
import eu.darkbot.api.PluginApiImpl;
import eu.darkbot.api.events.EventManager;
import eu.darkbot.impl.managers.AttackApiImpl;

public class DarkBotPluginApiImpl extends PluginApiImpl {

    public DarkBotPluginApiImpl(Main main) {
        super(main, //BotAPI & PluginAPI
                BackpageManager.class,
                EntityList.class,
                EventManager.class,
                FeatureRegistry.class,
                FlashResManager.class,
                HeroManager.class,
                SlotBarsProxy.class,
                Drive.class,
                PetManager.class,
                RepairManager.class,
                MapManager.class,
                StatsManager.class,
                AttackApiImpl.class,

                //facades
                LogMediator.class,
                ChatProxy.class,
                ChrominProxy.class,
                EscortProxy.class,
                EternalGateProxy.class,
                EternalBlacklightProxy.class,
                BoosterProxy.class);
    }

    public void addInstance(API.Singleton apiInstance) {
        this.singletons.add(apiInstance);
    }
}
