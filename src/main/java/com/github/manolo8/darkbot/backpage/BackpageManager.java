package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.HangarManager;
import com.github.manolo8.darkbot.utils.Base64Utils;
import com.github.manolo8.darkbot.utils.Time;

import java.net.HttpURLConnection;
import java.net.URL;

public class BackpageManager extends Thread {
    private final Main main;
    private final HangarManager hangarManager;
    private static final int SECOND = 1000, MINUTE = 60 * SECOND;

    private static final String[] ACTIONS = new String[] {
            "internalStart", "internalDock", "internalAuction", "internalGalaxyGates", "internalPilotSheet"
    };
    private static String getRandomAction() {
        return ACTIONS[(int) (Math.random() * ACTIONS.length)];
    }

    private String sid;
    private String instance;

    private long sidLastUpdate = System.currentTimeMillis();
    private long sidNextUpdate = sidLastUpdate;
    private int sidStatus = -1;
    private boolean checkDrones = false;

    public BackpageManager(Main main) {
        super("BackpageManager");
        this.main = main;
        this.hangarManager = new HangarManager(main, this);
        start();
    }

    @Override
    public void run() {
        while (true) {
            Time.sleep(5000);

            if (isInvalid()) {
                sidStatus = -1;
                continue;
            }


            if (System.currentTimeMillis() > sidNextUpdate) {
                int waitTime = sidCheck();
                sidLastUpdate = System.currentTimeMillis();
                sidNextUpdate = sidLastUpdate + (int) (waitTime + waitTime * Math.random());
            }

            if (checkDrones) {
                hangarManager.checkDrones();
                checkDrones = false;
            }


            if (sidStatus == 302) break;
        }
    }

    public void checkDronesAfterKill() {
        this.checkDrones = true;
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
        return getConnection("indexInternal.es?action=" + getRandomAction()).getResponseCode();
    }

    public HttpURLConnection getConnection(String params) throws Exception{
        if (isInvalid()) throw new UnsupportedOperationException("Can't connect when sid is invalid");
        HttpURLConnection conn = (HttpURLConnection) new URL(this.instance + params)
                .openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("Cookie", "dosid=" + this.sid);
        return conn;
    }

    public String getDataInventory(String params){
        String data = null;
        try {
            HttpURLConnection conn = getConnection(params);
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            data = Base64Utils.base64Decode(conn.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
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
