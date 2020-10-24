package eu.darkbot.api.managers;

import eu.darkbot.utils.Time;
import org.jetbrains.annotations.Nullable;

import java.net.HttpURLConnection;
import java.net.URI;

public interface BackpageManager {

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
     *
     * @param params
     * @return
     */
    HttpURLConnection getConnection(String params);

    /**
     *
     * @param params
     * @param minWait
     * @return
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
