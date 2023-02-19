package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginIssue;
import com.github.manolo8.darkbot.utils.Time;
import com.github.manolo8.darkbot.utils.http.Http;
import com.github.manolo8.darkbot.utils.http.Method;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.google.gson.Gson;
import eu.darkbot.api.extensions.Task;
import eu.darkbot.api.managers.BackpageAPI;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.util.Timer;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
public class BackpageManager extends Thread implements BackpageAPI {
    public static final Gson GSON = new Gson();

    public static final Pattern RELOAD_TOKEN_PATTERN = Pattern.compile("reloadToken=([^\"]+)");
    protected static final String[] ACTIONS = new String[]{
            "internalStart", "internalDock", "internalAuction", "internalGalaxyGates", "internalPilotSheet"};

    protected static class SidStatus {
        private static final int NO_SID = -1, ERROR = -2, UNKNOWN = -3;
    }

    public final LegacyHangarManager legacyHangarManager;
    public final HangarManager hangarManager;
    public final GalaxyManager galaxyManager;
    public final DispatchManager dispatchManager;
    public final AuctionManager auctionManager;
    public final NovaManager novaManager;

    protected final Main main;
    protected String sid, instance;
    protected URI instanceURI;
    protected List<Task> tasks = new ArrayList<>();

    protected long lastRequest;
    protected long sidLastUpdate = System.currentTimeMillis();
    protected long sidNextUpdate = sidLastUpdate;
    protected Timer refreshTimer = Timer.get(300_000L);

    protected long checkDrones = Long.MAX_VALUE;
    protected int sidStatus = -1;

    private int userId;
    private Optional<LoginData> loginData;

    private final Gson gson = new Gson();

    public BackpageManager(Main main, ConfigAPI configAPI) {
        super("BackpageManager");
        this.main = main;
        this.legacyHangarManager = new LegacyHangarManager(main, this);
        this.hangarManager = new HangarManager(this);
        this.galaxyManager = new GalaxyManager(this);
        this.dispatchManager = new DispatchManager(this, configAPI);
        this.auctionManager = new AuctionManager(this, configAPI);
        this.novaManager = new NovaManager(this);
        setDaemon(true);
    }

    private static String getRandomAction() {
        return ACTIONS[(int) (Math.random() * ACTIONS.length)];
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        while (true) {
            Time.sleep(100);

            synchronized (main.pluginHandler.getBackgroundLock()) {
                for (Task task : tasks) {
                    try {
                        task.onBackgroundTick();
                    } catch (Throwable e) {
                        main.featureRegistry.getFeatureDefinition(task)
                                .getIssues()
                                .handleTickFeatureException(PluginIssue.Level.WARNING, e);
                    }
                }
            }

            // For apis that support login, we can re-login if invalid
            if (Main.API.hasCapability(GameAPI.Capability.LOGIN)
                    && (isInvalid() || sidStatus == 302)
                    && refreshTimer.tryActivate()) {
                Main.API.handleRelogin();
                continue;
            }

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

            synchronized (main.pluginHandler.getBackgroundLock()) {
                for (Task task : tasks) {
                    try {
                        task.onTickTask();
                    } catch (Throwable e) {
                        main.featureRegistry.getFeatureDefinition(task)
                                .getIssues()
                                .handleTickFeatureException(PluginIssue.Level.WARNING, e);
                    }
                }
            }
        }
    }

    public void setLoginData(LoginData loginData) {
        if (this.loginData != null)
            throw new IllegalStateException("LoginData can be assigned only once!");

        this.loginData = Optional.ofNullable(loginData);
        this.isInvalid();
    }

    public void checkDronesAfterKill() {
        this.checkDrones = System.currentTimeMillis();
    }

    private boolean isInvalid() {
        if (loginData != null && loginData.isPresent()) {
            LoginData ld = loginData.get();

            this.sid = ld.getSid();
            this.userId = ld.getUserId();
            if (!Objects.equals(this.instance, ld.getFullUrl())) {
                this.instance = ld.getFullUrl();
                this.instanceURI = tryParse(this.instance);
            }

        } else {
            this.sid = main.statsManager.sid;
            this.userId = main.statsManager.userId;
            if (!Objects.equals(this.instance, main.statsManager.instance)) {
                this.instance = main.statsManager.instance;
                this.instanceURI = tryParse(this.instance);
            }
        }
        return sid == null || instance == null || sid.isEmpty() || instance.isEmpty() || this.userId == 0;
    }

    private URI tryParse(String uri) {
        if (uri == null || uri.isEmpty()) return null;
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            return null;
        }
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

    public HttpURLConnection getConnection(String path, int minWait) throws Exception {
        Time.sleep(lastRequest + minWait - System.currentTimeMillis());
        return getConnection(path);
    }

    public HttpURLConnection getConnection(String params) throws Exception {
        if (!isInstanceValid()) throw new UnsupportedOperationException("Can't connect when sid is invalid");
        HttpURLConnection conn = (HttpURLConnection) new URL(this.instance + params)
                .openConnection();
        conn.setConnectTimeout(30_000);
        conn.setReadTimeout(30_000);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("User-Agent", eu.darkbot.util.http.Http.getDefaultUserAgent());
        conn.setRequestProperty("Cookie", "dosid=" + this.sid);
        lastRequest = System.currentTimeMillis();
        return conn;
    }

    public Http getConnection(String params, Method method, int minWait) {
        Time.sleep(lastRequest + minWait - System.currentTimeMillis());
        return getConnection(params, method);
    }

    public Http getConnection(String params, Method method) {
        if (!isInstanceValid()) throw new UnsupportedOperationException("Can't connect when sid is invalid");
        return Http.create(this.instance + params, method)
                .setRawHeader("Cookie", "dosid=" + this.sid)
                .addSupplier(() -> lastRequest = System.currentTimeMillis());
    }

    @Deprecated
    public String getDataInventory(String params) {
        return legacyHangarManager.getDataInventory(params);
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
            case SidStatus.UNKNOWN:
                return "--";
            case SidStatus.ERROR:
                return "ERR";
            case 200:
                return "OK";
            case 302:
                return "KO";
            default:
                return sidStatus + "";
        }
    }

    public Gson getGson() {
        return GSON;
    }

    @Override
    public boolean isInstanceValid() {
        // Only check against local sid & instance variables, since stats manager ones are
        // updated in the main thread, while the variables here are updated on the background
        // thread every tick
        return sid != null && instance != null && !sid.isEmpty() && !instance.isEmpty() && userId != 0;
    }

    @Override
    public String getSidStatus() {
        return sidStat();
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getSid() {
        return sid;
    }

    @Override
    public URI getInstanceURI() {
        return instanceURI;
    }

    @Override
    public Instant getLastRequestTime() {
        return Instant.ofEpochMilli(lastRequest);
    }

    @Override
    public void updateLastRequestTime() {
        lastRequest = System.currentTimeMillis();
    }

    @Override
    public Optional<String> findReloadToken(@NotNull String body) {
        return Optional.ofNullable(getReloadToken(body));
    }
}
