package eu.darkbot.api.utils;

public class NativeAction {

    private static long toActionLong(short message, short wParam, int lParam, boolean after) {
        return toActionLong(message, wParam, (short) lParam, (short) (lParam >> 16), after);
    }

    //message only 15bits
    private static long toActionLong(short message, short wParam, short lParamLow, short lParamHigh, boolean after) {
        long val = lParamLow;
        val |= (long) lParamHigh << 16;
        val |= (long) (wParam & 0xffff) << 32;
        val |= (long) (message & 0xffff) << 48;

        if (after) val |= (long) 1 << 63;
        else val &= ~((long) 1 << 63);

        return val;
    }

    public enum Mouse {
        CLICK(0x1FF), // KekkaPlayer only
        MOVE(0x200),
        DOWN(0x201),
        UP(0x202);

        private final short message;

        Mouse(int message) {
            this.message = (short) message;
        }

        public long of(int x, int y) {
            return toActionLong(message, (short) 1, (short) x, (short) y, false);
        }

        // can be used after text paste action
        public long after(int x, int y) {
            return toActionLong(message, (short) 1, (short) x, (short) y, true);
        }
    }

    public enum Key {
        CLICK(0x1FE), // KekkaPlayer only
        DOWN(0x100),
        UP(0x101),
        CHAR(0x102);

        private final short message;

        Key(int message) {
            this.message = (short) message;
        }

        public long of(int keyCode) {
            return toActionLong(message, (short) keyCode, 1, false);
        }

        public long after(int keyCode) {
            return toActionLong(message, (short) keyCode, 1, true);
        }
    }

    public static class MouseWheel {
        private static final short DELTA = 120;
        private static final short MESSAGE = 0x20A;

        public static long up(int x, int y) {
            return toActionLong(MESSAGE, DELTA, (short) x, (short) y, false);
        }

        public static long down(int x, int y) {
            return toActionLong(MESSAGE, (short) -DELTA, (short) x, (short) y, false);
        }
    }
}
