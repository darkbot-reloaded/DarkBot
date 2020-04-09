package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.EffectManager;
import com.github.manolo8.darkbot.core.objects.Clickable;
import com.github.manolo8.darkbot.core.objects.LocationInfo;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;

import java.util.HashMap;
import java.util.Map;

import static com.github.manolo8.darkbot.Main.API;

public class Entity extends Updatable {

    public Main main;

    public int id;

    public LocationInfo locationInfo;
    public Clickable clickable;

    public boolean removed;

    public ObjArray traits;

    public Map<String, Object> metadata;

    public Entity() {
        this.locationInfo = new LocationInfo(0);
        this.clickable = new Clickable();
        this.traits = ObjArray.ofVector();
    }

    public Entity(int id) {
        this();
        this.id = id;
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

        int id = API.readMemoryInt(address + 56);
        long container = API.readMemoryLong(address + 96);

        return container != mapAddress || this.id != id;
    }

    @Override
    public void update() {
        locationInfo.update();
    }

    @Override
    public void update(long address) {
        super.update(address);

        this.locationInfo.update(API.readMemoryLong(address + 64));
        this.traits.update(API.readMemoryLong(address + 48));

        traits.update();

        for (int c = 0; c < traits.size; c++) {
            long adr = traits.get(c);

            int radius = API.readMemoryInt(adr + 40);
            int priority = API.readMemoryInt(adr + 44);
            int enabled = API.readMemoryInt(adr + 48);

            if (radius >= 0 && radius < 4000 && priority > -4 && priority < 1000 && (enabled == 1 || enabled == 0)) {
                clickable.update(adr);
                break;
            }
        }
    }

    public boolean hasEffect(EffectManager.Effect effect) {
        return main != null && main.effectManager.hasEffect(this, effect);
    }

    public boolean hasEffect(int effect) {
        return main != null && main.effectManager.hasEffect(this, effect);
    }

    public void added(Main main) {
        this.main = main;
        removed = false;
    }

    public void removed() {
        removed = true;
    }

    public void setMetadata(String key, Object value) {
        if (metadata == null) metadata = new HashMap<>();
        this.metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        if (metadata == null) return null;
        return this.metadata.get(key);
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
