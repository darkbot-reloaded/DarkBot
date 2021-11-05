package com.github.manolo8.darkbot.core.utils.factory;

import com.github.manolo8.darkbot.core.entities.Entity;

@FunctionalInterface
public interface EntityBuilder {

    /**
     * Creates an instance of the entity
     * @param id The id of the entity
     * @param address The address of the entity
     * @return An entity instance
     */
    Entity createEntity(int id, long address);

    /**
     * Get a more specialized builder for this specific address
     * @param address The address of the object
     * @return Often returns the same builder, in special cases it returns a more specific builder
     */
    default EntityBuilder get(long address) {
        return this;
    }

}
