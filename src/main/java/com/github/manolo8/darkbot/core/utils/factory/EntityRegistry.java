package com.github.manolo8.darkbot.core.utils.factory;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.utils.Lazy;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.github.manolo8.darkbot.Main.API;

public class EntityRegistry {
    private final Map<Long, EntityBuilder> cachedTypes       = new HashMap<>();
    private final Map<EntityFactory, Lazy<Entity>> listeners = new EnumMap<>(EntityFactory.class);
    private final Lazy<Entity> fallback                      = new Lazy.NoCache<>();

    private final Main main;
    private final Consumer<Entity> onEntityCreate;

    public EntityRegistry(Main main, Consumer<Entity> onEntityCreate, Consumer<Entity> onDefault) {
        this.main = main;
        this.onEntityCreate = onEntityCreate;
        this.fallback.add(onDefault);
    }

    public void add(EntityFactory type, Consumer<Entity> consumer) {
        listeners.computeIfAbsent(type, k -> new Lazy.NoCache<>()).add(consumer);
    }

    public boolean remove(EntityFactory type, Consumer<Entity> consumer) {
        Lazy<Entity> l = listeners.get(type);
        return l != null && l.remove2(consumer);
    }

    public void clearCache() {
        cachedTypes.clear();
    }

    public void sendEntity(int id, long address) {
        EntityBuilder type = cachedTypes.computeIfAbsent(API.readMemoryLong(address + 0x10),
                l -> EntityFactory.find(address)).get(address);
        if (address == main.hero.address || address == main.hero.pet.address) return;

        Entity entity = type.createEntity(id, address);
        if (entity == null) return;
        entity.added(main);
        entity.update();
        onEntityCreate.accept(entity);

        if (type instanceof EntityFactory)
            listeners.getOrDefault(type, fallback).send(entity);
        else
            fallback.send(entity);
    }

}