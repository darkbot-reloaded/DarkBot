package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.extensions.Task;
import eu.darkbot.utils.Time;

import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;

/**
 * API to manage, connect to backpage of the game.
 * {@link BackpageAPI} should be called only from {@link Task} thread.
 * @see com.github.manolo8.darkbot.core.itf.Task
 */
public interface BackpageAPI extends API {

    String getSid();

    /**
     * Returns instance {@link URI}
     * for example: {@code https://int1.darkorbit.com/}
     */
    URI getInstanceURI();

    /**
     * @return last request time.
     */
    Instant getLastRequestTime();

    /**
     * Returns connection with current instance + params
     * and with cookie dosid.
     *
     * @param params query to be added
     */
    HttpURLConnection getConnection(String params) throws Exception;

    /**
     * Returns connection with current instance + params
     * and with cookie dosid.
     * <p>
     * If minWait(ms) has not passed since last action, will sleep the difference.
     */
    default HttpURLConnection getConnection(String params, int minWait) throws Exception {
        Time.sleep(getLastRequestTime().toEpochMilli() + minWait - System.currentTimeMillis());
        return getConnection(params);
    }

    /**
     * Use random string anyway.
     * {@code
     *      UUID.randomUUID().toString().replaceAll("-", "");
     * }
     *
     * @param body which will be searched for reload token
     * @return reload token or {@link Optional#empty()} if wasn't found
     */
    Optional<String> findReloadToken(String body);
}
