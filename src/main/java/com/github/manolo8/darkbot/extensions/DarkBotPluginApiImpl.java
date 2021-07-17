package com.github.manolo8.darkbot.extensions;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.github.manolo8.darkbot.backpage.FlashResManager;
import com.github.manolo8.darkbot.core.manager.*;
import com.github.manolo8.darkbot.core.objects.facades.*;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.EntityList;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.modules.utils.NpcAttacker;
import eu.darkbot.api.PluginApiImpl;
import com.github.manolo8.darkbot.core.manager.EventBroker;

public class DarkBotPluginApiImpl extends PluginApiImpl {

    public DarkBotPluginApiImpl(Main main) {
        addInstance(main, Main.API, StarManager.getInstance());
        addImplementations(
                BackpageManager.class,
                EntityList.class,
                EventBroker.class,
                FeatureRegistry.class,
                FlashResManager.class,
                HeroManager.class,
                SlotBarsProxy.class,
                Drive.class,
                PetManager.class,
                RepairManager.class,
                MapManager.class,
                StatsManager.class,
                NpcAttacker.class);
    }

}
