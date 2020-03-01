package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.entities.galaxy.GalaxyInfo;
import com.github.manolo8.darkbot.backpage.entities.galaxy.SpinGate;

import javax.xml.parsers.DocumentBuilderFactory;

public class GalaxyManager {

    private GalaxyInfo galaxyInfo;
    private Main main;
    private long lastGatesUpdate;

    GalaxyManager(Main main) {
        this.main = main;
        this.galaxyInfo = new GalaxyInfo();
    }

    public GalaxyInfo getGalaxyInfo() {
        return galaxyInfo;
    }

    public void performGateSpin(SpinGate gate, boolean multiplier, int spinAmount, int minWait) {
        String params = createLink("multiEnergy") + gate.getParam();

        if (galaxyInfo.getSamples() != null && galaxyInfo.getSamples() > 0) params = params + "&sample=1";
        if (multiplier) params = params + "&multiplier=1";
        if (spinAmount > 4) params = params + "&spinamount=" + spinAmount;

        handleRequest(params, -1, minWait);
    }

    public void updateGalaxyInfo(int expiryTime) {
        handleRequest(createLink("init"), expiryTime, 2500);
    }

    private String createLink(String action) {
        return "flashinput/galaxyGates.php?userID=" + main.hero.id + "&action=" + action + "&sid=" + main.statsManager.sid;
    }

    private void handleRequest(String params, int expiryTime, int minWait) {
        if (System.currentTimeMillis() > lastGatesUpdate + expiryTime) {
            try {
                galaxyInfo.update(DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder()
                        .parse(main.backpage.getConnection(params, minWait).getInputStream())
                        .getDocumentElement());

                lastGatesUpdate = System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}