package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.entities.galaxy.GalaxyGate;
import com.github.manolo8.darkbot.backpage.entities.galaxy.GalaxyInfo;
import com.github.manolo8.darkbot.utils.http.Method;

import javax.xml.parsers.DocumentBuilderFactory;

public class GalaxyManager {

    private final Main main;
    private final GalaxyInfo galaxyInfo;
    private long lastGatesUpdate;

    GalaxyManager(Main main) {
        this.main = main;
        this.galaxyInfo = new GalaxyInfo();
    }

    public GalaxyInfo getGalaxyInfo() {
        return galaxyInfo;
    }

    /**
     * Spins gg energy into a gate
     * @param gate The gate to spin
     * @param useMultiAt Minimum amount to use multiplier, 2, 3, 4, 5 or 6
     * @param spinAmount Amount to spin, 1, 5, 10 or 100
     * @param minWait Minimum time to wait since last request
     * @return if the request was filled successfully
     */
    public boolean spinGate(GalaxyGate gate, int useMultiAt, int spinAmount, int minWait) {
        boolean useMultiplier = galaxyInfo.getGate(gate).getMultiplier() >= useMultiAt;
        return spinGate(gate, useMultiplier, spinAmount, minWait);
    }

    /**
     * Spins gg energy into a gate
     * @param gate The gate to spin
     * @param multiplier If multiplier should be used
     * @param spinAmount Amount to spin, 1, 5, 10 or 100
     * @param minWait Minimum time to wait since last request
     * @return if the request was filled successfully
     */
    public boolean spinGate(GalaxyGate gate, boolean multiplier, int spinAmount, int minWait) {
        String params = getLink("multiEnergy", false) + gate.getParam();

        if (galaxyInfo.getSamples() != null && galaxyInfo.getSamples() > 0) params += "&sample=1";
        if (multiplier)     params += "&multiplier=1";
        if (spinAmount > 4) params += "&spinamount=" + spinAmount;

        return Boolean.TRUE.equals(handleRequest(params, -1, minWait));
    }

    /**
     * Place the gate on your x-1 map
     * @param gate The gate to place
     * @param minWait Minimum time to wait since last request
     * @return if the request was filled successfully
     */
    public boolean placeGate(GalaxyGate gate, int minWait) {
        return Boolean.TRUE.equals(handleRequest(getLink("setupGate", true) + gate.getIdParam(), -1, minWait));
    }

    /**
     * @param expiryTime only update if within
     * @return null if update wasn't required (non-expired), true if updated ok, false if update failed
     */
    public Boolean updateGalaxyInfos(int expiryTime) {
        return handleRequest(getLink("init", false), expiryTime, 1000);
    }

    void initIfEmpty() {
        if (galaxyInfo.getGates().isEmpty())
            updateGalaxyInfos(2000);
    }


    @Deprecated  // Has no return status, impossible to know if ok or failed.
    public void prepareGate(GalaxyGate gate, int minWait) {
        placeGate(gate, minWait);
    }

    @Deprecated // Has no return status, impossible to know if ok or failed.
    public void performGateSpin(GalaxyGate gate, int useMultiAt, int spinAmount, int minWait) {
        spinGate(gate, useMultiAt, spinAmount, minWait);
    }

    @Deprecated // Has no return status, impossible to know if ok or failed.
    public void performGateSpin(GalaxyGate gate, boolean multiplier, int spinAmount, int minWait) {
        spinGate(gate, multiplier, spinAmount, minWait);
    }

    @Deprecated // Has no return status, impossible to know if ok or failed.
    public void updateGalaxyInfo(int expiryTime) {
        updateGalaxyInfos(expiryTime);
    }

    private String getLink(String action, boolean isInverted) {
        return "flashinput/galaxyGates.php?userID=" + main.hero.id
                + (isInverted ? "&sid=" + main.statsManager.sid + "&action=" + action
                : "&action=" + action + "&sid=" + main.statsManager.sid);
    }

    private Boolean handleRequest(String params, int expiryTime, int minWait) {
        if (System.currentTimeMillis() > lastGatesUpdate + expiryTime) {
            try {
                galaxyInfo.update(main.backpage.getConnection(params, Method.GET, minWait)
                        .addSupplier(() -> lastGatesUpdate = System.currentTimeMillis())
                        .consumeInputStream(inputStream -> DocumentBuilderFactory
                                .newInstance()
                                .newDocumentBuilder()
                                .parse(inputStream)
                                .getDocumentElement()));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return null;
    }
}