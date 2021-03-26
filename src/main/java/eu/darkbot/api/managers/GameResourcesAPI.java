package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public interface GameResourcesAPI extends API.Singleton {

    /**
     * @return used in-game {@link Locale}
     */
    @Nullable Locale getLanguage();

    /**
     * @param translationId of the translation
     * @return translation of given parameter or {@link Optional#empty()} if doesnt exists
     */
    Optional<String> findTranslation(String translationId);

    //todo get images etc...
}
