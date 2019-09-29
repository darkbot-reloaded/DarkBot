package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.entities.galaxy.GalaxyInfo;
import com.github.manolo8.darkbot.backpage.entities.galaxy.SpinGate;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.net.HttpURLConnection;

public class GalaxyManager {

    private GalaxyInfo galaxyInfo;
    private BackpageManager backpageManager;
    private Main main;
    private long lastGatesUpdate;

    GalaxyManager(Main main, BackpageManager backpageManager) {
        this.main = main;
        this.backpageManager = backpageManager;
        this.galaxyInfo = new GalaxyInfo();
    }

    public GalaxyInfo getGalaxyInfo() {
        return galaxyInfo;
    }

    public void performGateSpin(SpinGate gate, boolean multiplier, int spinAmount, int minWait) {
        String params = "flashinput/galaxyGates.php?userID=" + main.hero.id + "&action=multiEnergy&sid=" + main.statsManager.sid + gate.getParam();

        if (galaxyInfo.getSamples() != null && galaxyInfo.getSamples() > 0) params = params + "&sample=1";
        if (multiplier) params = params + "&multiplier=1";
        if (spinAmount > 4) params = params + "&spinamount=" + spinAmount;

        handleRequest(params, -1, minWait);
    }

    public void updateGalaxyInfo(int expiryTime) {
        handleRequest("flashinput/galaxyGates.php?userID=" + main.hero.id + "&action=init&sid=" + main.statsManager.sid, expiryTime, 2500);
    }

    private void handleRequest(String params, int expiryTime, int minWait) {
        if (System.currentTimeMillis() > lastGatesUpdate + expiryTime) {
            try {
                SAXReader reader = new SAXReader();

                HttpURLConnection conn = backpageManager.getConnection(params, minWait);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                Document document = reader.read(conn.getInputStream());
                if (document.getRootElement() == null) return;
                galaxyInfo.updateGalaxyInfo(document.getRootElement());
                lastGatesUpdate = System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}