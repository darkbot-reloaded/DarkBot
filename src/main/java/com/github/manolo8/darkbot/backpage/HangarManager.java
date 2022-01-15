package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.entities.Drone;
import com.github.manolo8.darkbot.backpage.entities.Item;
import com.github.manolo8.darkbot.backpage.entities.ItemInfo;
import com.github.manolo8.darkbot.backpage.entities.ShipInfo;
import com.github.manolo8.darkbot.backpage.hangar.Hangar;
import com.github.manolo8.darkbot.backpage.hangar.HangarResponse;
import com.github.manolo8.darkbot.core.itf.Tickable;
import com.github.manolo8.darkbot.utils.Base64Utils;
import com.github.manolo8.darkbot.utils.Time;
import com.github.manolo8.darkbot.utils.http.Method;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class HangarManager implements Tickable {
    private final Gson gson = new Gson();

    @Deprecated
    private final LegacyHangarManager legacyHangarManager;

    private final Main main;
    private final BackpageManager backpage;

    private HangarResponse hangarList;
    private HangarResponse currentHangar;

    private volatile long updateHangarListEvery = -1, updateCurrentHangarEvery = -1;
    private long lastHangarListUpdate, lastCurrentHangarUpdate;


    public HangarManager(Main main, BackpageManager backpage) {
        this.main = main;
        this.backpage = backpage;

        this.legacyHangarManager = backpage.legacyHangarManager;
    }

    @Override
    public void tick() {
        try {
            if (updateHangarListEvery != -1 &&
                    (lastHangarListUpdate + updateCurrentHangarEvery) < System.currentTimeMillis()) {
                long timer = System.currentTimeMillis();
                updateHangarList();
                lastHangarListUpdate = System.currentTimeMillis();
                updateHangarListEvery = -1;

                System.out.println("HangarList updated in: " + (System.currentTimeMillis() - timer) + "ms");
                Time.sleep(500);
            }

            if (updateCurrentHangarEvery != -1 &&
                    (lastCurrentHangarUpdate + updateCurrentHangarEvery) < System.currentTimeMillis()) {
                long timer = System.currentTimeMillis();
                updateCurrentHangar();
                lastCurrentHangarUpdate = System.currentTimeMillis();
                updateCurrentHangarEvery = -1;

                System.out.println("CurrentHangar updated in: " + (System.currentTimeMillis() - timer) + "ms");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HangarResponse getHangarList() {
        return hangarList;
    }

    public HangarResponse getCurrentHangar() {
        return currentHangar;
    }

    /**
     * Request hangar list to be updated within a certain timeframe.
     * This method should be repeatedly called to request updates
     * @param millis The maximum time to wait
     */
    public void requestUpdateCurrentHangar(long millis) {
        if (millis < 0) return;
        this.updateCurrentHangarEvery = millis;
    }

    /**
     * Request current hangar to be updated within a certain timeframe.
     * This method should be repeatedly called to request updates
     * @param millis The maximum time to wait
     */
    public void requestUpdateHangarList(long millis) {
        if (millis < 0) return;
        this.updateHangarListEvery = millis;
    }

    public void updateCurrentHangar() throws Exception {
        if (hangarList == null) updateHangarList();

        for (Hangar hangar : hangarList.getData().getRet().getHangars())
            if (hangar.isActive()) {
                this.currentHangar = getHangarResponseById(hangar.getHangarId());
                break;
            }
    }

    public void updateHangarList() throws Exception {
        this.hangarList = deserializeHangar(getInputStream("getHangarList", new JsonObject()));
    }

    public HangarResponse getHangarResponseById(int hangarId) throws Exception {
        JsonObject paramObj = new JsonObject();
        JsonObject hangarObj = new JsonObject();

        hangarObj.addProperty("hi", hangarId);
        paramObj.add("params", hangarObj);

        return deserializeHangar(getInputStream("getHangar", paramObj));
    }

    public InputStream getInputStream(String action, JsonObject json) throws IOException {
        return backpage.getConnection("flashAPI/inventory.php", Method.POST)
                .setRawParam("action", action)
                .setParam("params", Base64Utils.encode(json.toString()))
                .getInputStream();
    }

    private HangarResponse deserializeHangar(InputStream in) throws Exception {
        HangarResponse hangar = gson.fromJson(Base64Utils.decode(in), HangarResponse.class);
        in.close();

        if (hangar.getData().map != null) {
            String[] lootIds = hangar.getData().map.get("lootIds");

            hangar.getData().getRet().getItemInfos()
                    .forEach(itemInfo -> itemInfo.setLocalizationId(lootIds[itemInfo.getLootId()]));

            hangar.getData().map = null;
        }

        return hangar;
    }

    /* For backwards compatibility, keep methods from legacy hangar manager */
    @Deprecated
    public boolean changeHangar(String hangarId) {
        return legacyHangarManager.changeHangar(hangarId);
    }

    @Deprecated
    public Boolean checkDrones() {
        return legacyHangarManager.checkDrones();
    }

    @Deprecated
    public void updateHangars() {
        legacyHangarManager.updateHangars();
    }

    @Deprecated
    public String getActiveHangar() {
        return legacyHangarManager.getActiveHangar();
    }

    @Deprecated
    public void updateHangarData(int expiryTime) {
        legacyHangarManager.updateHangarData(expiryTime);
    }

    @Deprecated
    public List<Drone> getDrones() {
        return legacyHangarManager.getDrones();
    }

    @Deprecated
    public List<Item> getItems() {
        return legacyHangarManager.getItems();
    }

    @Deprecated
    public List<ItemInfo> getItemInfos() {
        return legacyHangarManager.getItemInfos();
    }

    @Deprecated
    public List<ShipInfo> getShipInfos() {
        return legacyHangarManager.getShipInfos();
    }

    @Deprecated
    public List<String> getTypes() {
        return legacyHangarManager.getTypes();
    }

    @Deprecated
    public List<String> getLootIds() {
        return legacyHangarManager.getLootIds();
    }
}
