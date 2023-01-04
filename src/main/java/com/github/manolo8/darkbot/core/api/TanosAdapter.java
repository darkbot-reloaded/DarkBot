package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.core.utils.pathfinder.RectangleImpl;
import com.github.manolo8.darkbot.utils.MathUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkTanos;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.managers.OreAPI;

import static com.github.manolo8.darkbot.Main.API;

public class TanosAdapter extends GameAPIImpl<
        DarkTanos,
        DarkTanos,
        DarkTanos,
        ByteUtils.ExtraMemoryReader,
        DarkTanos,
        TanosAdapter.DirectInteractionManager> {

    private static final String SELECT_MAP_ASSET = "MapAssetNotificationTRY_TO_SELECT_MAPASSET";


    public TanosAdapter(StartupParams params,
                        DirectInteractionManager di,
                        DarkTanos tanos,
                        BotInstaller botInstaller) {
        super(params, tanos, tanos, tanos, new ByteUtils.ExtraMemoryReader(tanos, botInstaller), tanos, di,
                GameAPI.Capability.LOGIN,
                GameAPI.Capability.INITIALLY_SHOWN,
                GameAPI.Capability.CREATE_WINDOW_THREAD,
                GameAPI.Capability.DIRECT_ENTITY_SELECT,
                GameAPI.Capability.DIRECT_MOVE_SHIP,
                GameAPI.Capability.DIRECT_COLLECT_BOX,
                GameAPI.Capability.DIRECT_REFINE,
                GameAPI.Capability.DIRECT_USE_ITEM,
                GameAPI.Capability.DIRECT_CALL_METHOD);
    }

    @Override
    public boolean isUseItemSupported() {
        return true;
    }

    @Override
    public boolean useItem(Item item) {
        return direct.tanos.useItem(0, item.id, 0, 0);
    }

    public static class DirectInteractionManager extends GameAPI.NoOpDirectInteraction {

        private final Main main;
        private final DarkTanos tanos;
        private final BotInstaller botInstaller;

        public DirectInteractionManager(Main main, DarkTanos tanos, BotInstaller botInstaller) {
            this.main = main;
            this.tanos = tanos;
            this.botInstaller = botInstaller;
        }

        @Override
        public int getVersion() {
            return 0;
        }

        @Override
        public void setMaxFps(int maxFps) {
            throw new UnsupportedOperationException();
        }

        /**
         * Translate between coordinate systems
         *
         * @param value The value to transform, in map coordinates
         * @param mapSize In-game map width/height, in map coordinates
         * @param clientSize Window width/height, in pixels
         * @return value converted to
         */
        private int translate(double value, double mapSize, int clientSize) {
            return (int) (value / mapSize * (double) clientSize);
        }

        /**
         * Ensure val is between 0 and max. If it isn't, soft clamp to 0-5% of appropriate corner
         * @param val The value to clamp
         * @param max The window in which to clamp the value
         * @return value if between 0 and max, otherwise 0 + 5% of max or max - 5% of max, randomly.
         */
        private int softClamp(int val, int max) {
            if (val >= 0 && val <= max) return val;
            int offset = (int) (Math.random() * max * 0.05);
            return val < 0 ? offset : max - offset;
        }

        @Override
        public void selectEntity(Entity entity) {
            if (entity.clickable.isInvalid()) return;

            RectangleImpl viewBounds = HeroManager.instance.main.mapManager.viewBounds.polygon.getBounds();
            double bx = viewBounds.getX(), by = viewBounds.getY();
            double bw = viewBounds.getWidth(), bh = viewBounds.getHeight();
            int cw = MapManager.clientWidth, ch = MapManager.clientHeight;

            // The location of entity in the screen, possibly shifted to be inside the screen
            int entityScreenX = softClamp(translate(entity.getX() - bx, bw, cw), cw);
            int entityScreenY = softClamp(translate(entity.getY() - by, bh, ch), ch);

            // An angle of max +-45 deg ensures if the entity is inside, mouse pos is also inside
            double angle = entity.angleTo(viewBounds.getCenterX(), viewBounds.getCenterY())
                    + (Math.random() - 0.5) * MathUtils.HALF_PI;
            double dist = Math.random() * entity.clickable.defRadius;

            int diffX = translate(-Math.cos(angle) * dist, bw, cw);
            int diffY = translate(-Math.sin(angle) * dist, bh, ch);

            sendNotification(
                    SELECT_MAP_ASSET,
                    entity.getId(),
                    (int) entity.getX(), (int) entity.getY(),
                    entityScreenX + diffX, entityScreenY + diffY,
                    entityScreenX, entityScreenY,
                    entity.clickable.defRadius);
        }

        private void sendNotification(String notification, int... args) {
            if (botInstaller.screenManagerAddress.get() == 0) return;

            long[] tagged = new long[args.length];
            for (int i = 0; i < args.length; i++) {
                tagged[i] = ByteUtils.tagInteger(args[i]);
            }

            tanos.sendNotification(botInstaller.screenManagerAddress.get(), notification, tagged);
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
            callMethod(index, arguments);
            return true;
        }
    }
}
