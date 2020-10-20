package eu.darkbot.api.managers;

import eu.darkbot.utils.Time;

import java.net.HttpURLConnection;

public interface BackpageManager {
    /**
     *
     * @return
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
    String findReloadToken(String body);
}
