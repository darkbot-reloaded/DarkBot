package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.entities.galaxy.GalaxyGate;
import com.github.manolo8.darkbot.backpage.entities.galaxy.GalaxyInfo;

import javax.xml.parsers.DocumentBuilderFactory;

public class GalaxyManager {

    private Main main;
    private GalaxyInfo galaxyInfo;
    private long lastGatesUpdate;

    GalaxyManager(Main main) {
        this.main = main;
        this.galaxyInfo = new GalaxyInfo();
    }

    public GalaxyInfo getGalaxyInfo() {
        return galaxyInfo;
    }

    public void prepareGate(GalaxyGate gate, int minWait) {
        handleRequest(getLink("setupGate", true) + gate.getIdParam(), -1, minWait);
    }

    public void performGateSpin(GalaxyGate gate, int useMultiAt, int spinAmount, int minWait) {
        boolean useMultiplier = galaxyInfo.getGate(gate).getMultiplier() >= useMultiAt;
        performGateSpin(gate, useMultiplier, spinAmount, minWait);
    }

    public void performGateSpin(GalaxyGate gate, boolean multiplier, int spinAmount, int minWait) {
        String params = getLink("multiEnergy", false) + gate.getParam();

        if (galaxyInfo.getSamples() != null && galaxyInfo.getSamples() > 0) params += "&sample=1";
        if (multiplier)     params += "&multiplier=1";
        if (spinAmount > 4) params += "&spinamount=" + spinAmount;

        handleRequest(params, -1, minWait);
    }

    public void updateGalaxyInfo(int expiryTime) {
        handleRequest(getLink("init", false), expiryTime, 1000);
    }

    void initIfEmpty() {
        if (galaxyInfo.getGates().isEmpty()) updateGalaxyInfo(2000);
    }

    private String getLink(String action, boolean isInverted) {
        return "flashinput/galaxyGates.php?userID=" + main.hero.id
                + (isInverted ? "&sid=" + main.statsManager.sid + "&action=" + action
                : "&action=" + action + "&sid=" + main.statsManager.sid);
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