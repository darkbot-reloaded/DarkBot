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
 * @see Task
 */
public interface BackpageAPI extends API.Singleton {

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
     * Returns connection with current {@link #getInstanceURI()} & path, with the sessionID cookie.
     *
     * @param path URL path & query parameters to append to {@link #getInstanceURI()}
     */
    HttpURLConnection getConnection(String path) throws Exception;

    /**
     * Returns connection with current {@link #getInstanceURI()} & path, with the sessionID cookie.
     *
     *
     * @param path URL path & query parameters to append to {@link #getInstanceURI()}
     * @param minWait Minimum time to wait since the last request in milliseconds.
     *                If the last request was over {@param minWait} ms ago, behavior
     *                is identical to {@link #getConnection(String)}, otherwise it will
     *                first sleep until enough time has passed.
     */
    default HttpURLConnection getConnection(String path, int minWait) throws Exception {
        Time.sleep(getLastRequestTime().toEpochMilli() + minWait - System.currentTimeMillis());
        return getConnection(path);
    }

    /**
     * Random string representing the reload token of the loaded page
     *
     * @param body which will be searched for reload token
     * @return reload token or {@link Optional#empty()} if wasn't found
     */
    Optional<String> findReloadToken(String body);
}
