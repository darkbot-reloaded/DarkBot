package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.utils.Base64Utils;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.Time;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackpageManager extends Thread {
    private final Main main;
    public final HangarManager hangarManager;
    public final GalaxyManager galaxyManager;
    private static final int SECOND = 1000, MINUTE = 60 * SECOND;

    private static final String[] ACTIONS = new String[] {
            "internalStart", "internalDock", "internalAuction", "internalGalaxyGates", "internalPilotSheet"
    };

    private static String getRandomAction() {
        return ACTIONS[(int) (Math.random() * ACTIONS.length)];
    }

    private String sid;
    private String instance;
    private long lastRequest;

    private long sidLastUpdate = System.currentTimeMillis();
    private long sidNextUpdate = sidLastUpdate;
    private int sidStatus = -1;
    private long checkDrones = Long.MAX_VALUE;

    private List<Task> tasks;

    public BackpageManager(Main main) {
        super("BackpageManager");
        this.main = main;
        this.hangarManager = new HangarManager(main, this);
        this.galaxyManager = new GalaxyManager(main);
        start();
    }

    @Override
    public void run() {
        while (true) {
            Time.sleep(100);

            if (isInvalid()) {
                sidStatus = -1;
                continue;
            }

            if (System.currentTimeMillis() > sidNextUpdate) {
                int waitTime = sidCheck();
                sidLastUpdate = System.currentTimeMillis();
                sidNextUpdate = sidLastUpdate + (int) (waitTime + waitTime * Math.random());
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
                        task.tick();
                    } catch (Throwable e) {
                        main.featureRegistry.getFeatureDefinition(task)
                                .getIssues()
                                .addWarning(I18n.get("bot.issue.feature.failed_to_tick"), IssueHandler.createDescription(e));
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
            sidStatus = -2;
            e.printStackTrace();
            return 5 * MINUTE;
        }
        return 10 * MINUTE;
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
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("Cookie", "dosid=" + this.sid);
        lastRequest = System.currentTimeMillis();
        return conn;
    }

    public String getDataInventory(String params) {
        String data = null;
        try {
            HttpURLConnection conn = getConnection(params, 2500);
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            data = Base64Utils.base64Decode(conn.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public String getReloadToken(InputStream input) {
        Pattern p = Pattern.compile("reloadToken=([^\"]+)");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(input))) {
            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                Matcher m = p.matcher(currentLine);
                if (m.find()) {
                    return m.group(1);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public synchronized String sidStatus() {
        return sidStat() + (sidStatus != -1 && sidStatus != 302 ?
                " " + Time.toString(System.currentTimeMillis() - sidLastUpdate) + "/" +
                        Time.toString(sidNextUpdate - sidLastUpdate) : "");
    }

    private String sidStat() {
        switch (sidStatus) {
            case -1: return "--";
            case -2: return "ERR";
            case 200: return "OK";
            case 302: return "KO";
            default: return sidStatus + "";
        }
    }

}
