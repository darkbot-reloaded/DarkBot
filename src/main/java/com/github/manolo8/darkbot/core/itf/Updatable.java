package com.github.manolo8.darkbot.core.itf;

/**
 * Represents an in-game object that has an address, and can be updated.
 */
public abstract class Updatable implements NativeUpdatable {

    public long address;

    public abstract void update();

    public final void updateIfChanged(long address) {
        if (this.address != address) {
            update(address);
        }
    }

    public void update(long address) {
        this.address = address;
    }

    @Override
    public long getAddress() {
        return address;
    }

    public boolean isValid() {
        return address != 0;
    }

    /**
     * Updatable which automatically calls update() after an address change.
     * Unless you want different logic in address updates and normal updates, you should use this.
     */
    public abstract static class Auto extends Updatable {
        @Override
        public void update(long address) {
            super.update(address);
            update();
        }
    }

    /**
     * Updatable which can report if any change has occurred when updating.
     * It also automatically calls update on address changes, like Auto.
     * Useful for SwfPtrCollection#sync
     */
    public abstract static class Reporting extends Updatable {

        public final boolean updateAndReport(long address) {
            if (this.address != address) {
                update(address);
                updateAndReport();
                return true;
            }
            return updateAndReport();
        }

        @Override
        public void update() {
            updateAndReport();
        }

        public abstract boolean updateAndReport();
    }

    public static class NoOp extends Updatable {

        @Override
        public void update() {}
    }
}
