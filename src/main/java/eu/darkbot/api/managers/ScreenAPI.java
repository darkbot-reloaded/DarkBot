package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.Entity;
import eu.darkbot.api.objects.Rectangle;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ScreenAPI extends API {

    //not sure where to place this
    <T extends Entity> Collection<T> getEntities(@NotNull Class<T> entity);

    int getClientWidth();
    int getClientHeight();// already in bounds

    Rectangle getViewBounds();
}
