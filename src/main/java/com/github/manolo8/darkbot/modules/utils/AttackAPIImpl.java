package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.NpcExtra;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.extensions.features.handlers.PrioritizedLaserHandler;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.selectors.PrioritizedSupplier;
import eu.darkbot.api.game.entities.Npc;
import eu.darkbot.api.game.items.ItemFlag;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.HeroItemsAPI;
import eu.darkbot.impl.managers.AbstractAttackImpl;
import org.jetbrains.annotations.Nullable;

public class AttackAPIImpl extends AbstractAttackImpl {

    private final Main main;
    private final PrioritizedLaserHandler laserHandler;

    public AttackAPIImpl(Main main, HeroItemsAPI heroItems, HeroAPI hero,
                         FeatureRegistry featureRegistry, ConfigAPI config) {
        super(heroItems, hero);
        this.main = main;

        this.laserHandler = featureRegistry.getPrioritizedHandlerOf(PrioritizedLaserHandler.class);
        this.laserHandler.setDefaultSupplier(new DefaultLaserSupplier(config));
    }

    @Override
    protected boolean isAttackViaSlotBarEnabled() {
        return true; //todo
    }

    @Override
    protected SelectableItem.Laser getBestLaserAmmo() {
        return laserHandler.getBestSupplier().get();
    }

    private class DefaultLaserSupplier implements PrioritizedSupplier<SelectableItem.Laser> {

        private final ConfigSetting<Boolean> rsbEnabled;
        private final ConfigSetting<Config.Loot.Sab> sabSettings;

        private long usedRsb;
        private boolean useRsb, useSab;

        public DefaultLaserSupplier(ConfigAPI config) {
            this.rsbEnabled = config.requireConfig("loot.rsb.enabled");
            this.sabSettings = config.requireConfig("loot.sab");
        }

        @Override
        public SelectableItem.Laser get() {
            return useRsb ? SelectableItem.Laser.RSB_75
                    : useSab ? SelectableItem.Laser.SAB_50
                    : SelectableItem.Laser.LCB_10;
            //todo change with config fields
        }

        @Override
        public @Nullable Priority getPriority() {
            return (useRsb = shouldRsb()) ? Priority.MODERATE
                    : (useSab = shouldSab()) ? Priority.LOW
                    : Priority.LOWEST;
        }

        private boolean shouldSab() {
            Npc target = getTargetAs(Npc.class);

            if (target == null || !sabSettings.getValue().ENABLED
                    || target.getInfo().hasExtraFlag(NpcExtra.NO_SAB)) return false;

            Config.Loot.Sab SAB = sabSettings.getValue();
            return hero.getHealth().shieldPercent() <= SAB.PERCENT
                    && target.getHealth().getShield() > SAB.NPC_AMOUNT
                    && (SAB.CONDITION == null || SAB.CONDITION.get(main).toBoolean());
        }

        private boolean shouldRsb() {
            Npc target = getTargetAs(Npc.class);
            if (target == null || !rsbEnabled.getValue()
                    || !target.getInfo().hasExtraFlag(NpcExtra.USE_RSB)) return false;

            boolean isReady = heroItems.getItem(SelectableItem.Laser.RSB_75, ItemFlag.USABLE, ItemFlag.READY).isPresent();

            if (isReady && usedRsb < System.currentTimeMillis() - 1000) usedRsb = System.currentTimeMillis();
            return isReady && usedRsb > System.currentTimeMillis() - 500;
        }
    }
}
