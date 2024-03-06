package com.github.manolo8.darkbot.extensions;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.github.manolo8.darkbot.backpage.FlashResManager;
import com.github.manolo8.darkbot.backpage.NativeBrowserImpl;
import com.github.manolo8.darkbot.config.ConfigHandler;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.manager.PetManager;
import com.github.manolo8.darkbot.core.manager.RepairManager;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.core.objects.facades.SlotBarsProxy;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.EntityList;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.modules.utils.AttackAPIImpl;
import com.github.manolo8.darkbot.utils.LegacyModules;
import eu.darkbot.impl.PluginApiImpl;
import eu.darkbot.impl.decorators.ListenerDecorator;
import eu.darkbot.impl.managers.EventBroker;
import eu.darkbot.impl.managers.I18n;

public class DarkBotPluginApiImpl extends PluginApiImpl {

    public DarkBotPluginApiImpl(Main main) {
        addInstance(main, main.params, StarManager.getInstance(), main.configManager);
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
                AttackAPIImpl.class,
                LegacyModules.class,
                I18n.class,
                ConfigHandler.class,
                NativeBrowserImpl.class);
        addDecorator(requireInstance(ListenerDecorator.class));
    }

}
