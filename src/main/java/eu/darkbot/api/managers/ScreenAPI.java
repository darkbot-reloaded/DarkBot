package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.objects.Rectangle;

public interface ScreenAPI extends API {

    int getClientWidth();

    int getClientHeight();

    Rectangle getViewBounds();
}
