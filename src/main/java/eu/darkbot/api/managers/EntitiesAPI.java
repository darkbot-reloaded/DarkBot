package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.Entity;
import eu.darkbot.api.objects.Obstacle;
import eu.darkbot.api.utils.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface EntitiesAPI extends API {

    /**
     * @return {@link Collection} of {@link Obstacle}
     */
    Collection<Obstacle> getObstacles();

    /**
     * @return reference to {@link Collection} of all entities
     */
    Collection<Entity> getAllEntities();

    /**
     * @return {@link Collection} of unknown entities
     */
    Collection<Entity> getUnknownEntities();

    /**
     * Use {@code Entity.class} to get {@link Collection} of all entities.
     *
     * @param entityType to get collection of
     * @return {@link Collection} of given entity type
     */
    <T extends Entity>
    Collection<T> getEntities(Class<T> entityType);

    /**
     * Adds listeners on create or remove of the {@link Entity}
     * <p>
     * Remember to store references for given listeners!
     * Otherwise they will be garbage collected.
     *
     * @param onCreate {@link Listener} which can be null
     * @param onRemove {@link Listener} which can be null
     * @see Listener
     */
    void addListener(@Nullable Listener<Entity> onCreate,
                     @Nullable Listener<Entity> onRemove);

}
