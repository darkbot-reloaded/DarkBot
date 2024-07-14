package eu.darkbot.api.utils;

public class NativeAction {

    private static long toActionLong(short message, short wParam, int lParam, boolean after) {
        return toActionLong(message, wParam, (short) lParam, (short) (lParam >> 16), after);
    }

    private static long toActionLong(short message, short wParam, short lParamLow, short lParamHigh, boolean after) {
        long val = ((long) lParamHigh << 16) | (lParamLow & 0xFFFF);
        val |= ((long) wParam & 0xFFFF) << 32;
        val |= ((long) message & 0xFFFF) << 48;
        if (after) {
            val |= 1L << 63;
        }
        return val;
    }

    public enum Mouse {
        CLICK(0x1FF),
        MOVE(0x200),
        DOWN(0x201),
        UP(0x202);

        private final short message;

        Mouse(int message) {
            this.message = (short) message;
        }

        public long of(int x, int y) {
            return toActionLong(message, (short) 1, x, false);
        }

        public long after(int x, int y) {
            return toActionLong(message, (short) 1, x, true);
        }
    }

    public enum Key {
        CLICK(0x1FE),
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
            return toActionLong(MESSAGE, DELTA, x, false);
        }

        public static long down(int x, int y) {
            return toActionLong(MESSAGE, (short) -DELTA, x, false);
        }
    }
}
