package eu.darkbot.api.managers;

import java.util.Locale;
import java.util.Optional;

public interface GameResourcesAPI {

    /**
     * @return used in-game {@link Locale}
     */
    Locale getLanguage();

    /**
     * @param translationId of the translation
     * @return translation of given parameter or {@link Optional#empty()} if doesnt exists
     */
    Optional<String> getTranslation(String translationId);

    //todo get images etc...
}
