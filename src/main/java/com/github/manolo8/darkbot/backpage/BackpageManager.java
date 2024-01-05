package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.extensions.plugins.PluginIssue;
import com.github.manolo8.darkbot.utils.Time;
import com.github.manolo8.darkbot.utils.http.Http;
import com.github.manolo8.darkbot.utils.http.Method;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.google.gson.Gson;
import eu.darkbot.api.extensions.Task;
import eu.darkbot.api.managers.BackpageAPI;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.util.IOUtils;
import eu.darkbot.util.TimeUtils;
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
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
public class BackpageManager extends Thread implements BackpageAPI {
    public static final Gson GSON = new Gson();

    public static final Pattern RELOAD_TOKEN_PATTERN = Pattern.compile("reloadToken=([^\"]+)");

    private static final String SHOP_PATH = "ajax/shop.php";
    private static final String[] ACTIONS = new String[]{
            "internalDock", "internalDock&tpl=internalDockAmmo", "internalSkylab"};

    public final LegacyHangarManager legacyHangarManager;
    public final HangarManager hangarManager;
    public final AuctionManager auctionManager;
    public final NovaManager novaManager;

    protected final Main main;
    protected String sid, instance;
    protected URI instanceURI;
    protected List<Task> tasks = new ArrayList<>();

    protected long lastRequest;
    protected long sidLastUpdate = System.currentTimeMillis();
    protected long sidNextUpdate = sidLastUpdate;
    protected final Timer refreshTimer = Timer.get(TimeUtils.MINUTE * 5);
    protected final Timer shopTimer = Timer.get(TimeUtils.MINUTE * 20);

    protected long checkDrones = Long.MAX_VALUE;

    private int userId;
    private Optional<LoginData> loginData;
    private Status status = Status.UNKNOWN;

    public BackpageManager(Main main, ConfigAPI configAPI) {
        super("BackpageManager");
        this.main = main;
        this.legacyHangarManager = new LegacyHangarManager(main, this);
        this.hangarManager = new HangarManager(this);
        this.auctionManager = new AuctionManager(this, configAPI);
        this.novaManager = new NovaManager(this);
        setDaemon(true);
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        while (true) {
            Time.sleep(100);

            tickTasks(Task::onBackgroundTick);

            // tick tasks only on valid SID?
            if (checkSidValid()) {
                hangarManager.tick();

                // is it even useful now?
                //checkDrones();
                tickTasks(Task::onTickTask);
            }
        }
    }

    private void tickTasks(Consumer<Task> tickConsumer) {
        synchronized (main.pluginHandler.getBackgroundLock()) {
            for (Task task : tasks) {
                try {
                    tickConsumer.accept(task);
                } catch (Throwable e) {
                    main.featureRegistry.getFeatureDefinition(task)
                            .getIssues()
                            .handleTickFeatureException(PluginIssue.Level.WARNING, e);
                }
            }
        }
    }

    private boolean checkSidValid() {
        // basically isInvalid() should never return true with LOGIN capability
        if (isInvalid()) {
            status = Status.NO_SID;
            return false;
        } else if (status == Status.NO_SID) {
            status = Status.UNKNOWN;
        }

        if (sidNextUpdate < System.currentTimeMillis()) {
            try {
                if (shopTimer.isInactive()) {
                    String action = ACTIONS[(int) (Math.random() * ACTIONS.length)];
                    status = Status.of(getResponseCode(action));
                } else {
                    // do not follow redirects
                    int shopResponse = getResponseCode("internalDock&tpl=internalDockAmmo");

                    status = Status.of(shopResponse);
                    if (status == Status.VALID) {
                        HttpURLConnection conn = postHttp(SHOP_PATH, 3000)
                                .setParam("action", "purchase")
                                .setParam("category", "battery")
                                .setParam("itemId", "ammunition_laser_lcb-10")
                                .setParam("amount", 1)
                                .setParam("selectedName", "")
                                .setHeader("Referer", instance + "indexInternal.es?action=internalDock&tpl=internalDockAmmo")
                                .getConnection();

                        status = Status.of(conn.getResponseCode());
                        if (status == Status.VALID && IOUtils.read(conn.getInputStream(), true)
                                .contains("redirectToLogin")) {
                            status = Status.INVALID;
                        }
                    }
                }
            } catch (Exception e) {
                status = Status.ERROR;
                e.printStackTrace();
            }

            int waitTime = (status == Status.ERROR ? 5 : 8) * Time.MINUTE;
            sidLastUpdate = System.currentTimeMillis();
            sidNextUpdate = sidLastUpdate + (int) (waitTime + waitTime * Math.random());
        }

        // only try to relogin on `302` response code or `redirectToLogin` message while buying ammo
        if (Main.API.hasCapability(Capability.LOGIN) && status.shouldRelogin() && refreshTimer.tryActivate()) {
            Main.API.handleRelogin(true);
            sidNextUpdate = System.currentTimeMillis();
        }

        return status == Status.VALID;
    }

    private int getResponseCode(String action) throws Exception {
        return getConnection("indexInternal.es?action=" + action).getResponseCode();
    }

    private void checkDrones() {
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

    public HttpURLConnection getConnection(String path, int minWait) throws Exception {
        Time.sleep(lastRequest + minWait - System.currentTimeMillis());
        return getConnection(path);
    }

    public HttpURLConnection getConnection(String params) throws Exception {
        if (!isInstanceValid()) throw new UnsupportedOperationException("Can't connect when sid is invalid");
        if (params.toLowerCase(Locale.ROOT).contains(SHOP_PATH))
            shopTimer.activate();

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
        if (params.toLowerCase(Locale.ROOT).contains(SHOP_PATH))
            shopTimer.activate();

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
        return status + (status.displayTime() ?
                " " + Time.toString(System.currentTimeMillis() - sidLastUpdate) + "/" +
                        Time.toString(sidNextUpdate - sidLastUpdate) : "");
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
        return status.toString();
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

    private enum Status {
        UNKNOWN("?"),
        NO_SID("--"),
        VALID("OK"),
        ERROR("ERR"),
        INVALID("KO");

        private final String status;

        Status(String status) {
            this.status = status;
        }

        boolean shouldRelogin() {
            return this == INVALID;
        }

        boolean displayTime() {
            return this != UNKNOWN && this != NO_SID;
        }

        @Override
        public String toString() {
            return status;
        }

        static Status of(int responseCode) {
            return responseCode == 200 ? VALID : responseCode == 302 ? INVALID : ERROR;
        }
    }
}
