package com.github.manolo8.darkbot.core.itf;

/**
 * Represents an in-game object that has an address, and can be updated.
 */
public abstract class Updatable implements NativeUpdatable {

    public long address;

    public abstract void update();

    public void update(long address) {
        this.address = address;
    }

    @Override
    public long getAddress() {
        return address;
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

        public boolean updateAndReport(long address) {
            boolean addressChanged = this.address != address;
            super.update(address);
            return updateAndReport() || addressChanged;
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
