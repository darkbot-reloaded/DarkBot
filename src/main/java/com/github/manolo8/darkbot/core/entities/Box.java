package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.config.BoxInfo;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.utils.Offsets;
import eu.darkbot.util.Timer;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.github.manolo8.darkbot.Main.API;

public class Box extends Entity implements eu.darkbot.api.game.entities.Box {
    private static final Map<String, Long> PAST_BOXES = new HashMap<>();

    private final Timer invalidTimer = Timer.get(1_500);

    private long collectedUntil;
    private int retries = 0;

    public String type, hash;

    public BoxInfo boxInfo;


    public Box(int id, long address) {
        super(id);
        this.update(address);

        // ignore box for 1.5sec if reappears with same hash in 1.5sec
        PAST_BOXES.values().removeIf(v -> v <= System.currentTimeMillis());
        if (PAST_BOXES.containsKey(hash))
            invalidTimer.activate();
    }

    @Override
    public void removed() {
        super.removed();

        // add only if is not a `fake` box
        if (invalidTimer.isInactive())
            PAST_BOXES.put(hash, System.currentTimeMillis() + 1_500);
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public String getTypeName() {
        return type;
    }

    @Override
    public eu.darkbot.api.config.types.BoxInfo getInfo() {
        return boxInfo;
    }

    public boolean isCollected() {
        return invalidTimer.isActive() || removed || System.currentTimeMillis() < collectedUntil;
    }

    @Override
    public boolean tryCollect() {
        if (API.hasCapability(GameAPI.Capability.DIRECT_COLLECT_BOX)) {
            API.collectBox(this);
            setCollected();
            return true;
        }

        if (trySelect(false)) {
            setCollected();
            return true;
        }

        return false;
    }

    public void setCollected() {
        collectedUntil = System.currentTimeMillis() + getNextWait();
        retries++;
    }

    public int getRetries() {
        return retries;
    }

    @Override
    public @Nullable Instant isCollectedUntil() {
        return Instant.ofEpochMilli(collectedUntil);
    }

    public int getNextWait() {
        return retries % 3 == 0 ? 50 : // one every 3 is an "instant retry"
                retries < 5 ? retries * 1_000 : retries * 60_000; // After 5 retries consider box bugged
    }

    @Override
    public void update() {
        super.update();
        clickable.update();
    }

    @Override
    public void update(long address) {
        super.update(address);

        if (traits.size == 0) {
            boxInfo = new BoxInfo();
            return;
        }

        type = Offsets.getTraitAssetId(traits.get(0));
        hash = API.readMemoryString(address, 160);

        if (type.length() > 5) type = type.split(",")[0].replace("box_", "").replace("_box", "");

        boxInfo = ConfigEntity.INSTANCE.getOrCreateBoxInfo(type);
    }

    public static class Ore extends Box implements eu.darkbot.api.game.entities.Ore {

        public Ore(int id, long address) {
            super(id, address);
        }
    }

    public static class Beacon extends Box implements eu.darkbot.api.game.entities.Box.BeaconBox {

        public Beacon(int id, long address) {
            super(id, address);
        }
    }
}
