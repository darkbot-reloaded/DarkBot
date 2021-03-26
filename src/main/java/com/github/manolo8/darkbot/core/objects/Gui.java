package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.API;

import java.util.function.Consumer;

import static com.github.manolo8.darkbot.Main.API;

public class Gui extends Updatable implements API, eu.darkbot.api.objects.Gui {

    protected final Point pos = new Point();
    protected final Point size = new Point();
    protected final Point minimized = new Point();

    public long addressInfo;
    public boolean visible;

    public int x;
    public int y;
    public int width;
    public int height;

    protected boolean isTweening; // If it's in the middle of an animation
    protected long time;
    protected long update;

    private ObjArray tempArray;
    private ObjArray tempChildArray;

    public void update() {
        if (address == 0) return;
        pos.update(API.readMemoryLong(addressInfo + 9 * 8));
        size.update(API.readMemoryLong(addressInfo + 10 * 8));
        // 11 * 8 = FeatureDefinitionVo
        // 12 * 8 = help text
        // 13 * 8 = tool tip
        minimized.update(API.readMemoryLong(addressInfo + 14 * 8));

        width = (int) Math.round(size.x);
        height = (int) Math.round(size.y);
        // Set pos relative to window size
        x = (int) Math.round((MapManager.clientWidth - size.x) * 0.01 * pos.x);
        y = (int) Math.round((MapManager.clientHeight - size.y) * 0.01 * pos.y);

        visible = API.readMemoryBoolean(addressInfo + 32); // Maximized
        // API.readMemoryBoolean(addressInfo + 36); // Toggle maximize (set to true/false when pressing H to show/hide)
        // API.readMemoryBoolean(addressInfo + 40); // Maximized changed (set to true when toggling maximized)
        // API.readMemoryBoolean(addressInfo + 44); // Settings on server
        // API.readMemoryBoolean(addressInfo + 48); // show on top

        isTweening = API.readMemoryBoolean(address + 0xC4);
    }

    @Override
    public void update(long address) {
        if (address == 0) {
            reset();
        } else {
            super.update(address);
            this.addressInfo = API.readMemoryLong(address + 496);
            this.update = System.currentTimeMillis();
        }
    }

    public void reset() {
        this.address = 0;
        this.visible = false;
        this.height = 0;
        this.width = 0;
        this.update = 0;
    }

    public boolean lastUpdatedIn(long time) {
        return update != 0 && System.currentTimeMillis() - update > time;
    }

    @Override
    public void click(int plusX, int plusY) {
        API.mouseClick(x + plusX, y + plusY);
    }

    @Override
    public void hover(int plusX, int plusY) {
        API.mouseMove(x + plusX, y + plusY);
    }

    public boolean show(boolean value) {
        if (trySetShowing(value)) {
            if (minimized.address != 0) API.mouseClick((int) minimized.x + 5, (int) minimized.y + 5);
            return false;
        }
        return isAnimationDone();
    }

    /**
     * @param value Desired visibility status
     * @return If action should be taken to change the visibility status
     */
    public boolean trySetShowing(boolean value) {
        if (value != visible && isAnimationDone()) {
            time = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public boolean isAnimationDone() {
        return !isTweening && System.currentTimeMillis() - 1000 > time;
    }

    /**
     * Doing action for each child of sprite after reading a wrapper.
     *
     * @param spriteAddress address of Sprite or object which extends Sprite
     * @param consumer to be executed every child.
     */
    public void forEachSpriteChild(long spriteAddress, Consumer<Long> consumer) {
        if (tempChildArray == null) tempChildArray = ObjArray.ofSprite();

        tempChildArray.update(spriteAddress);
        tempChildArray.forEach(l -> consumer.accept(API.readMemoryLong(l, 216)));
    }

    /**
     * Get child of sprite
     * index of sprite must be known.
     *
     * @param childIndex set -1 to get last.
     */
    public long getSpriteChild(long spriteAddress, int childIndex) {
        return API.readMemoryLong(getSpriteChildWrapper(spriteAddress, childIndex), 216);
    }

    /**
     * Get child of sprite wrapped in a object
     * index of sprite must be known.
     *
     * @param childIndex set -1 to get last.
     */
    public long getSpriteChildWrapper(long spriteAddress, int childIndex) {
        if (tempChildArray == null) tempChildArray = ObjArray.ofSprite();

        tempChildArray.update(spriteAddress);
        return childIndex != -1 ? tempChildArray.getPtr(childIndex) : tempChildArray.getLast();
    }

    /**
     * Sprite Object with id at 168 offset.
     */
    public long getSpriteElement(int elementsListId, int elementId) {
        long listAddr = getElementsList(elementsListId);
        return getSpriteElement(listAddr, elementId);
    }

    public long getSpriteElement(long elementsListAddress, int elementId) {
        if (elementsListAddress == 0) return 0;

        tempArray.update(API.readMemoryLong(elementsListAddress, 184));
        for (int i = 0; i < tempArray.getSize(); i++)
            if (API.readMemoryInt(tempArray.getPtr(i), 168) == elementId)
                return tempArray.get(i);

        return 0;
    }

    /**
     * An Array of Sprites Array with ids.
     */
    public long getElementsList(int elementsListId) {
        if (tempArray == null) tempArray = ObjArray.ofArrObj();

        tempArray.update(API.readMemoryLong(address, 400));
        for (int i = 0; i < tempArray.getSize(); i++)
            if (API.readMemoryInt(tempArray.getPtr(i), 172) == elementsListId)
                return tempArray.get(i);

        return 0;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public boolean setVisible(boolean visible) {
        return show(visible);
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }
}