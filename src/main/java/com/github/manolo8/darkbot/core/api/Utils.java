package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.core.utils.pathfinder.RectangleImpl;
import com.github.manolo8.darkbot.utils.MathUtils;

import java.util.Set;

public class Utils {
    public static final String SELECT_MAP_ASSET = "MapAssetNotificationTRY_TO_SELECT_MAPASSET";

    public static long[] createSelectEntityArgs(Entity entity) {
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

        return tagIntegers(
                entity.getId(),
                (int) entity.getX(), (int) entity.getY(),
                entityScreenX + diffX, entityScreenY + diffY,
                entityScreenX, entityScreenY,
                entity.clickable.defRadius);
    }

    private static long[] tagIntegers(int... args) {
        long[] tagged = new long[args.length];
        for (int i = 0; i < args.length; i++) {
            tagged[i] = ByteUtils.tagInteger(args[i]);
        }

        return tagged;
    }

    /**
     * Translate between coordinate systems
     *
     * @param value The value to transform, in map coordinates
     * @param mapSize In-game map width/height, in map coordinates
     * @param clientSize Window width/height, in pixels
     * @return value converted to
     */
    private static int translate(double value, double mapSize, int clientSize) {
        return (int) (value / mapSize * (double) clientSize);
    }

    /**
     * Ensure val is between 0 and max. If it isn't, soft clamp to 0-5% of appropriate corner
     * @param val The value to clamp
     * @param max The window in which to clamp the value
     * @return value if between 0 and max, otherwise 0 + 5% of max or max - 5% of max, randomly.
     */
    private static int softClamp(int val, int max) {
        if (val >= 0 && val <= max) return val;
        int offset = (int) (Math.random() * max * 0.05);
        return val < 0 ? offset : max - offset;
    }

    public interface SignatureChecker extends GameAPI.DirectInteraction {

        Set<String> signatureCache();

        default boolean checkGotoMethod(GameAPI.Memory mem, BotInstaller installer) {
            String signature = "26(267726?2?)42407911700";
            if (signatureCache().contains(signature)) return true;

            long eventManager = mem.readLong(installer.screenManagerAddress.get(), 200);
            return checkSignature(false, signature, 10, eventManager);
        }

        default boolean checkSignature(boolean checkName, String signature, int index, long object) {
            if (index <= 2 || !ByteUtils.isValidPtr(object)) return false;
            if (!signatureCache().contains(signature)) {
                // -1 or -2 == memory read error, 0 == invalid signature, 1 == valid
                int ret = checkMethodSignature(object, index, checkName, signature);

                if (ret == 1) signatureCache().add(signature);
                else if (ret == 0) {
                    throw new InvalidNativeSignature("Invalid flash method signature! " + signature);
                } else return false;
            }

            return true;
        }
    }

}
