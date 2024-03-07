package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.EffectManager;
import com.github.manolo8.darkbot.core.objects.Clickable;
import com.github.manolo8.darkbot.core.objects.LocationInfo;
import com.github.manolo8.darkbot.core.objects.swf.FlashListLong;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.core.utils.TraitPattern;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.github.manolo8.darkbot.Main.API;

public class Entity extends Updatable implements eu.darkbot.api.game.entities.Entity {
    // was 800 before but added possibility that user has increased attack range by zephyr's momentum ability
    public static final int DEFAULT_CLICK_RADIUS = 900;

    public Main main;
    public Map<String, Object> metadata;
    public LocationInfo locationInfo = new LocationInfo();
    public Clickable clickable = new Clickable();
    public FlashListLong traits = FlashListLong.ofVector();

    public int id;
    public boolean removed;

    public Entity() {
    }

    public Entity(int id) {
        this.id = id;
    }

    public Entity(int id, long address) {
        this(id);
        this.update(address);
    }

    public int getId() {
        return id;
    }

    public long getAddress() {
        return address;
    }

    public LocationInfo getLocationInfo() {
        return locationInfo;
    }

    public boolean isInvalid(long mapAddress) {
        if (address == 0) return true;
        int  id        = API.readInt(address + 56);
        long container = API.readLong(address + 96);

        return container != mapAddress || this.id != id;
    }

    @Override
    public void update() {
        locationInfo.update();
    }

    @Override
    public void update(long address) {
        super.update(address);

        this.locationInfo.update(API.readLong(address + 64));
        this.traits.update(API.readLong(address + 48));

        this.clickable.update(findInTraits(TraitPattern::ofClickable));
    }

    protected long findInTraits(Predicate<Long> filter) {
        for (int i = 0; i < traits.size(); i++) {
            long ptr = traits.getLong(i);
            if (filter.test(ptr))
                return ptr;
        }
        return ByteUtils.NULL;
    }

    public boolean hasEffect(EffectManager.Effect effect) {
        return main != null && main.effectManager.hasEffect(this, effect);
    }

    public boolean hasEffect(int effect) {
        return main != null && main.effectManager.hasEffect(this, effect);
    }

    public void added(Main main) {
        this.main    = main;
        this.removed = false;
    }

    public void removed() {
        removed = true;
    }

    public void setMetadata(@NotNull String key, Object value) {
        if (metadata == null) metadata = new HashMap<>();
        this.metadata.put(key, value);
    }

    public Object getMetadata(@NotNull String key) {
        if (metadata == null) return null;
        return this.metadata.get(key);
    }

    @Override
    public String toString() {
        return id + getEffects().toString();
    }

    @Override
    public boolean isValid() {
        return !removed;
    }

    @Override
    public boolean isSelectable() {
        return clickable.enabled;
    }

    @Override
    public boolean trySelect(boolean tryAttack) {
        if (!isSelectable() || distanceTo(main.hero) > DEFAULT_CLICK_RADIUS) return false;

        API.selectEntity(this);
        return true; // We assume it was successful
    }

    @Override
    public Collection<Integer> getEffects() {
        return main == null ? Collections.emptySet() : main.effectManager.getEffects(this);
    }
}
