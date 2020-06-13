package com.github.manolo8.darkbot.core.utils.factory;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.utils.Lazy;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.github.manolo8.darkbot.Main.API;

public class EntityListener {
    private final Map<Long, EntityFactory> cachedTypes       = new HashMap<>();
    private final Map<EntityFactory, Lazy<Entity>> listeners = new EnumMap<>(EntityFactory.class);
    private Main main;

    public void setMain(Main main) {
        this.main = main;
    }

    public void add(EntityFactory type, Consumer<Entity> consumer) {
        getListener(type).add(consumer);
    }

    public void addForEach(Consumer<Entity> consumer) {
        listeners.forEach((f, e) -> add(f, consumer));
    }

    public boolean remove(EntityFactory type, Consumer<Entity> consumer) {
        return getListener(type).remove(consumer);
    }

    public void clearCache() {
        if (!cachedTypes.isEmpty()) cachedTypes.clear();
    }

    //workaround for 5-2 station Class was recognized as unknown on x-1, x-8 map
    public void clearUnknownCache() {
        if (cachedTypes.isEmpty()) return;
        cachedTypes.entrySet().removeIf(entry -> entry.getValue() == EntityFactory.UNKNOWN);
    }


    public void sendEntity(int id, long address) {
        EntityFactory type = getEntityType(address);
        if (address == main.hero.address || address == main.hero.pet.address) return;

        Entity entity = type.createEntity(id, address);
        if (entity == null) return;
        entity.added(main);
        entity.update();

        if (main.isRunning()) entity.clickable.setRadius(0);

        getListener(type).send(entity);
    }

    private EntityFactory getEntityType(long address) {
        return cachedTypes.computeIfAbsent(API.readMemoryLong(address + 0x10),
                                           l -> EntityFactory.find(address)).get(address);
    }

    private Lazy<Entity> getListener(EntityFactory type) {
        return this.listeners.computeIfAbsent(type, k -> new Lazy.NoCache<>());
    }
}