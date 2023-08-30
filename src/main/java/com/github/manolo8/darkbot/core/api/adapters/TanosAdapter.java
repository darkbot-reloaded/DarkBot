package com.github.manolo8.darkbot.core.api.adapters;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.api.GameAPIImpl;
import com.github.manolo8.darkbot.core.api.InvalidNativeSignature;
import com.github.manolo8.darkbot.core.api.Utils;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkTanos;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.managers.OreAPI;

import java.util.HashSet;
import java.util.Set;

public class TanosAdapter extends GameAPIImpl<
        DarkTanos,
        DarkTanos,
        DarkTanos,
        ByteUtils.ExtraMemoryReader,
        DarkTanos,
        TanosAdapter.DirectInteractionManager> {

    public TanosAdapter(StartupParams params,
                        DirectInteractionManager di,
                        DarkTanos tanos,
                        BotInstaller botInstaller) {
        super(params, tanos, tanos, tanos, new ByteUtils.ExtraMemoryReader(tanos, botInstaller), tanos, di,
                Capability.LOGIN,
                Capability.INITIALLY_SHOWN,
                Capability.CREATE_WINDOW_THREAD,
                Capability.DIRECT_ENTITY_SELECT,
                Capability.DIRECT_MOVE_SHIP,
                Capability.DIRECT_COLLECT_BOX,
                Capability.DIRECT_REFINE,
                Capability.DIRECT_USE_ITEM,
                Capability.DIRECT_CALL_METHOD);
    }

    @Override
    public boolean isUseItemSupported() {
        return true;
    }

    @Override
    public boolean useItem(Item item) {
        if (direct.checkSignature(true, "23(sendRequest)(2626)1016221500",
                18, direct.botInstaller.connectionManagerAddress.get())) {
            return direct.tanos.useItem(direct.botInstaller.connectionManagerAddress.get(), item.id, 19, 0);
        }

        return false;
    }

    public static class DirectInteractionManager extends NoopAPIAdapter.NoOpDirectInteraction {

        private final Main main;
        private final DarkTanos tanos;
        private final BotInstaller botInstaller;
        private final Set<String> methodSignatureCache = new HashSet<>();

        public DirectInteractionManager(Main main, DarkTanos tanos, BotInstaller botInstaller) {
            this.main = main;
            this.tanos = tanos;
            this.botInstaller = botInstaller;
        }

        @Override
        public void selectEntity(Entity entity) {
            if (entity.clickable.isInvalid()) return;
            if (botInstaller.screenManagerAddress.get() == 0) return;

            long[] args = Utils.createSelectEntityArgs(entity);
            tanos.sendNotification(botInstaller.screenManagerAddress.get(), Utils.SELECT_MAP_ASSET, args);
        }

        @Override
        public void moveShip(Locatable dest) {
            callMethod(10, tanos.readLong(main.mapManager.eventAddress), (long) dest.getX(), (long) dest.getY());
        }

        @Override
        public void collectBox(Box box) {
            callMethod(10,
                    tanos.readLong(main.mapManager.eventAddress),
                    box.locationInfo.x(), box.locationInfo.y(), box.address, 0);
        }

        @Override
        public void refine(long refineUtilAddress, OreAPI.Ore oreType, int amount) {
            tanos.refine(refineUtilAddress, oreType.getId(), amount);
        }

        @Override
        public long callMethod(int index, long... arguments) {
            long[] args = new long[arguments.length - 1];
            System.arraycopy(arguments, 1, args, 0, args.length);
            return tanos.callMethod(arguments[0], index, args);
        }

        @Override
        public boolean callMethodChecked(boolean checkName, String signature, int index, long... arguments) {
            if (checkSignature(checkName, signature, index, arguments[0])) {
                callMethod(index, arguments);
                return true;
            }
            return false;
        }

        private boolean checkSignature(boolean checkName, String signature, int index, long object) {
            if (index <= 2 || !ByteUtils.isValidPtr(object)) return false;
            if (!methodSignatureCache.contains(signature)) {
                // -1 or -2 == memory read error, 0 == invalid signature, 1 == valid
                int ret = tanos.checkMethodSignature(object, index, checkName, signature);

                if (ret == 1) methodSignatureCache.add(signature);
                else if (ret == 0) {
                    throw new InvalidNativeSignature("Invalid flash method signature! " + signature);
                } else return false;
            }

            return true;
        }
    }
}
