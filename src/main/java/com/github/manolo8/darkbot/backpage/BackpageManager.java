package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.utils.Base64Utils;
import com.github.manolo8.darkbot.utils.Time;
import com.github.manolo8.darkbot.utils.http.Http;
import com.github.manolo8.darkbot.utils.http.Method;
import eu.darkbot.api.managers.BackpageAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackpageManager extends Thread implements BackpageAPI {
    public  static final Pattern RELOAD_TOKEN_PATTERN = Pattern.compile("reloadToken=([^\"]+)");
    protected static final String[] ACTIONS = new String[]{
            "internalStart", "internalDock", "internalAuction", "internalGalaxyGates", "internalPilotSheet"};

    protected static class SidStatus {
        private static final int NO_SID = -1, ERROR = -2, UNKNOWN = -3;
    }

    public final HangarManager hangarManager;
    public final LegacyHangarManager legacyHangarManager;
    public final GalaxyManager galaxyManager;

    protected final Main main;
    protected String sid, instance;
    protected List<Task> tasks;

    protected long lastRequest;
    protected long sidLastUpdate = System.currentTimeMillis();
    protected long sidNextUpdate = sidLastUpdate;
    protected long checkDrones = Long.MAX_VALUE;
    protected int sidStatus = -1;

    public BackpageManager(Main main) {
        super("BackpageManager");
        this.main = main;
        this.hangarManager = new HangarManager(main, this);
        this.legacyHangarManager = new LegacyHangarManager(main, this);
        this.galaxyManager = new GalaxyManager(main);
        setDaemon(true);
        start();
    }

    private static String getRandomAction() {
        return ACTIONS[(int) (Math.random() * ACTIONS.length)];
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        while (true) {
            Time.sleep(100);

            if (isInvalid()) {
                sidStatus = SidStatus.NO_SID;
                continue;
            } else if (sidStatus == SidStatus.NO_SID) {
                sidStatus = SidStatus.UNKNOWN;
            }

            this.hangarManager.tick();

            if (System.currentTimeMillis() > sidNextUpdate) {
                int waitTime = sidCheck();
                sidLastUpdate = System.currentTimeMillis();
                sidNextUpdate = sidLastUpdate + (int) (waitTime + waitTime * Math.random());
                galaxyManager.initIfEmpty();
            }

            if (System.currentTimeMillis() > checkDrones) {
                try {
                    boolean checked = hangarManager.checkDrones();

                    System.out.println("Checked/repaired drones, all successful: " + checked);

                    checkDrones = !checked ? System.currentTimeMillis() + 30_000 : Long.MAX_VALUE;
                } catch (Exception e) {
                    System.err.println("Failed to check & repair drones, retry in 5m");
                    checkDrones = System.currentTimeMillis() + 300_000;
                    e.printStackTrace();
                }
            }

            for (Task task : tasks) {
                synchronized (main.pluginHandler.getBackgroundLock()) {
                    try {
                        task.tickTask();
                    } catch (Throwable e) {
                        main.featureRegistry.getFeatureDefinition(task)
                                .getIssues()
                                .addWarning("bot.issue.feature.failed_to_tick", IssueHandler.createDescription(e));
                    }
                }
            }
        }
    }

    public void checkDronesAfterKill() {
        this.checkDrones = System.currentTimeMillis();
    }

    private boolean isInvalid() {
        this.sid = main.statsManager.sid;
        this.instance = main.statsManager.instance;
        return sid == null || instance == null || sid.isEmpty() || instance.isEmpty();
    }

    private int sidCheck() {
        try {
            sidStatus = sidKeepAlive();
        } catch (Exception e) {
            sidStatus = SidStatus.ERROR;
            e.printStackTrace();
            return 5 * Time.MINUTE;
        }
        return 10 * Time.MINUTE;
    }

    private int sidKeepAlive() throws Exception {
        return getConnection("indexInternal.es?action=" + getRandomAction(), 5000).getResponseCode();
    }

    public HttpURLConnection getConnection(String params, int minWait) throws Exception {
        Time.sleep(lastRequest + minWait - System.currentTimeMillis());
        return getConnection(params);
    }

    public HttpURLConnection getConnection(String params) throws Exception {
        if (isInvalid()) throw new UnsupportedOperationException("Can't connect when sid is invalid");
        HttpURLConnection conn = (HttpURLConnection) new URL(this.instance + params)
                .openConnection();
        conn.setConnectTimeout(30_000);
        conn.setReadTimeout(30_000);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("User-Agent", Http.getDefaultUserAgent());
        conn.setRequestProperty("Cookie", "dosid=" + this.sid);
        lastRequest = System.currentTimeMillis();
        return conn;
    }

    public Http getConnection(String params, Method method, int minWait) {
        Time.sleep(lastRequest + minWait - System.currentTimeMillis());
        return getConnection(params, method);
    }

    public Http getConnection(String params, Method method) {
        if (isInvalid()) throw new UnsupportedOperationException("Can't connect when sid is invalid");
        return Http.create(this.instance + params, method)
                .setRawHeader("Cookie", "dosid=" + this.sid)
                .addSupplier(() -> lastRequest = System.currentTimeMillis());
    }

    public String getDataInventory(String params) {
        try {
            return getConnection(params, Method.GET, 2500)
                    .setRawHeader("Content-Type", "application/x-www-form-urlencoded")
                    .consumeInputStream(Base64Utils::decode);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getReloadToken(InputStream input) {
        Matcher matcher = RELOAD_TOKEN_PATTERN.matcher("");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
            return br.lines()
                    .map(matcher::reset)
                    .filter(Matcher::find)
                    .map(m -> m.group(1))
                    .findFirst().orElse(null);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getReloadToken(String body) {
        Matcher m = RELOAD_TOKEN_PATTERN.matcher(body);
        return m.find() ? m.group(1) : null;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public synchronized String sidStatus() {
        return sidStat() + (sidStatus != SidStatus.NO_SID && sidStatus != 302 ?
                " " + Time.toString(System.currentTimeMillis() - sidLastUpdate) + "/" +
                        Time.toString(sidNextUpdate - sidLastUpdate) : "");
    }

    private String sidStat() {
        switch (sidStatus) {
            case SidStatus.NO_SID:
            case SidStatus.UNKNOWN: return "--";
            case SidStatus.ERROR: return "ERR";
            case 200: return "OK";
            case 302: return "KO";
            default: return sidStatus + "";
        }
    }

    @Override
    public String getSid() {
        return sid;
    }

    @Override
    public URI getInstanceURI() {
        return URI.create(instance);
    }

    @Override
    public Instant getLastRequestTime() {
        return Instant.ofEpochMilli(lastRequest);
    }

    @Override
    public Optional<String> findReloadToken(String body) {
        return Optional.ofNullable(getReloadToken(body));
    }
}
