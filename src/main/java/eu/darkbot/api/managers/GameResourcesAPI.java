package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

/**
 * API providing in-game resources, like translation keys
 *
 *
 */
public interface GameResourcesAPI extends API.Singleton {

    /**
     * @return used in-game {@link Locale}
     */
    @Nullable Locale getLanguage();

    /**
     * @param key of the translation
     * @return text in-game in the current language, or {@link Optional#empty()} if not found
     */
    Optional<String> findTranslation(String key);

    // TODO: provide a way to search text-to-key
}
