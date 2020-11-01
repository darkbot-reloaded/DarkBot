package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.objects.slotbars.CategoryBar;
import eu.darkbot.api.objects.slotbars.SlotBar;
import org.jetbrains.annotations.Nullable;

/**
 * API to get info about slot bars,
 * category bar, items etc.
 */
public interface SlotBarAPI extends API {

    /**
     * @return {@link CategoryBar}
     */
    CategoryBar getCategoryBar();

    /**
     * @return standard {@link SlotBar}
     */
    SlotBar getStandardBar();

    /**
     * @return premium {@link SlotBar} otherwise null if doesn't exists.
     */
    @Nullable
    SlotBar getPremiumBar();

    /**
     * @return pro action {@link SlotBar} otherwise null if doesn't exists.
     */
    @Nullable
    SlotBar getProActionBar();

    //maybe move search items here?
}
