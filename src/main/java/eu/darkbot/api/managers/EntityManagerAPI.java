package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.Entity;
import eu.darkbot.api.utils.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface EntityManagerAPI extends API {

    <T> Collection<T> getEntities(Class<T> entityType);

    Collection<Entity> getUnknownEntities();

    void addListener(@Nullable Listener<Entity> onCreate,
                     @Nullable Listener<Entity> onRemove);

}
