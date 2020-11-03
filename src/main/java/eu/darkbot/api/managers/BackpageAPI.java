package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.utils.Time;
import org.jetbrains.annotations.Nullable;

import java.net.HttpURLConnection;
import java.net.URI;

/**
 * API to manage, connect backpage of the game.
 * {@link BackpageAPI} should be called only from {@link eu.darkbot.api.plugin.Task} thread.
 */
public interface BackpageAPI extends API {

    String getSid();

    /**
     * Returns instance {@link URI}
     * for example: https://int1.darkorbit.com/
     */
    URI getInstanceURI();

    /**
     * @return last request time in milliseconds
     */
    long getLastRequestTime();

    /**
     * Returns connection with current instance + params
     * and with cookie dosid.
     *
     * @param params query to be added
     */
    HttpURLConnection getConnection(String params);

    /**
     * Returns connection with current instance + params
     * and with cookie dosid.
     *
     * If minWait(ms) has not passed since last action, will sleep the difference.
     */
    default HttpURLConnection getConnection(String params, int minWait) {
        Time.sleep(getLastRequestTime() + minWait - System.currentTimeMillis());
        return getConnection(params);
    }

    /**
     * Use random string anyway.
     * UUID.randomUUID().toString().replaceAll("-", "");
     *
     * @param body which will be searched for reload token
     * @return reload token or null if wasn't found
     */
    @Nullable
    String findReloadToken(String body);
}
