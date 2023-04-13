package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.backpage.dispatch.BiIntConsumer;
import com.github.manolo8.darkbot.backpage.dispatch.DispatchData;
import com.github.manolo8.darkbot.backpage.dispatch.Gate;
import com.github.manolo8.darkbot.backpage.dispatch.InProgress;
import com.github.manolo8.darkbot.backpage.dispatch.Retriever;
import com.github.manolo8.darkbot.utils.CaptchaHandler;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.util.IOUtils;
import org.intellij.lang.annotations.Language;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DispatchManager {
    private final BackpageManager backpageManager;
    private final DispatchData data;
    private final Map<String, Integer> collected;
    private final Map<String, Integer> lastCollected;
    private final Gson gson;
    private final CaptchaHandler captchaHandler;

    private long lastDispatcherUpdate;

    DispatchManager(BackpageManager backpageManager, ConfigAPI configAPI) {
        this.backpageManager = backpageManager;
        this.data = new DispatchData();
        this.collected = new HashMap<>();
        this.lastCollected = new HashMap<>();
        this.gson = backpageManager.getGson();
        this.captchaHandler = new CaptchaHandler(backpageManager, configAPI,
                "indexInternal.es?action=internalDispatch", "dispatch");
    }

    public DispatchData getData() {
        return data;
    }

    @Deprecated
    public Boolean update(int expiryTime) {
        return this.update((long) expiryTime);
    }

    /**
     * @param expiryTime only update if within
     * @return null if update wasn't required (non-expired), true if updated ok, false if update failed
     */
    public Boolean update(long expiryTime) {
        try {
            if (System.currentTimeMillis() <= lastDispatcherUpdate + expiryTime) return null;
            if (captchaHandler.isSolvingCaptcha()) return false;
            HttpURLConnection httpURLConnection = backpageManager.getHttp("indexInternal.es?action=internalDispatch").getConnection();
            String page = IOUtils.read(httpURLConnection.getInputStream());
            if (captchaHandler.needsCaptchaSolve(httpURLConnection.getURL(), page)) {
                System.out.println("DispatchManager: Captcha Detected");
                captchaHandler.solveCaptcha();
                return false;
            }
            lastDispatcherUpdate = System.currentTimeMillis();
            return InfoReader.updateAll(page, data);
        } catch (Exception e) {
            System.out.println("Exception updating dispatch data" + e);
            e.printStackTrace();
        }
        return false;
    }

    public Map<String, Integer> getCollected() {
        return collected;
    }

    public Map<String, Integer> getLastCollected() {
        return lastCollected;
    }

    public boolean hireRetriever(Retriever retriever) {
        if (retriever == null) return false;
        try {
            if (data.getAvailableSlots() <= 0) return false;
            if (retriever.getPermitCost() > data.getPermit()) {
                return handleResponse("Hire Retriever", retriever.getId(), "(ERROR) Can Not Hire Retriever, Not enough permits");
            }
            String response = backpageManager.postHttp("ajax/dispatch.php")
                    .setParam("command", "sendDispatch")
                    .setParam("dispatchId", retriever.getId())
                    .getContent();
            return handleResponse("Hired Dispatcher", retriever.getId(), response);
        } catch (Exception e) {
            System.out.println("Exception hiring dispatcher: " + e);
            e.printStackTrace();
        }
        return false;
    }

    public boolean collectInstant(InProgress progress) {
        if (progress == null) return false;
        try {
            System.out.println("Collecting Instant: Slot " + progress.getSlotId());
            if (data.getPrimeCoupons() <= 0)
                return handleResponse("Instant Collect", progress.getId(),
                        "(ERROR) Can Not Instant Collect, No Prime Coupon available for instant collection");
            String response = backpageManager.postHttp("ajax/dispatch.php")
                    .setParam("command", "instantComplete")
                    .setParam("dispatchId", progress.getId())
                    .setParam("dispatchRewardPackage", progress.getDispatchRewardPackage())
                    .setParam("slotId", progress.getSlotId())
                    .getContent();

            return handleResponse("Collected Retriever", progress.getId(), response);
        } catch (Exception e) {
            System.out.println("Exception collecting dispatcher: " + e);
            e.printStackTrace();
        }
        return false;
    }

    public boolean hireGate(Gate gate) {
        if (gate == null) return false;
        try {
            if (gate.getInProgress()) {
                return handleResponse("Hire Gate", gate.getName(), "(ERROR) This Gate in Progress, Can Not Start Same Gate");
            }
            if (data.getGateUnits() <= 0) {
                return handleResponse("Hire Gate ", gate.getName(), "(ERROR) Can not Hire Gate, Not enough GGEU");
            }

            String response = backpageManager.postHttp("ajax/dispatch.php")
                    .setParam("command", "sendGateDispatch")
                    .setParam("gateId", gate.getId())
                    .getContent();
            return handleResponse("Gate Started", gate.getName(), response);
        } catch (Exception e) {
            System.out.println("Exception dispatching gate: " + e);
            e.printStackTrace();
        }

        return false;
    }

    public boolean collectGate(Gate gate) {
        if (gate.getCollectable().equals("0")) return false;
        try {
            System.out.println("Collecting: Gate " + gate.getName());
            String response = backpageManager.postHttp("ajax/dispatch.php")
                    .setParam("command", "collectGateDispatch")
                    .setParam("gateId", gate.getId())
                    .getContent();

            gate.setInProgress(false);
            gate.setCollectable("0");
            gate.setTime("0");
            gate.setCost("0");

            return handleResponse("Collected Gate", gate.getName(), response);
        } catch (Exception e) {
            System.out.println("Exception collecting dispatcher: " + e);
            e.printStackTrace();
        }
        return false;
    }


    public boolean collect(InProgress progress) {
        if (progress.getCollectable().equals("0")) return false;
        try {
            System.out.println("Collecting: Slot " + progress.getSlotId());
            String response = backpageManager.postHttp("ajax/dispatch.php")
                    .setParam("command", "collectDispatch")
                    .setParam("slot", progress.getSlotId())
                    .getContent();

            return handleResponse("Collected Retriever", progress.getId(), response);
        } catch (Exception e) {
            System.out.println("Exception collecting dispatcher: " + e);
            e.printStackTrace();
        }
        return false;
    }

    private boolean handleResponse(String type, String id, String response) {
        boolean failed = response.contains("ERROR");
        if (!failed) {
            this.lastCollected.clear();
            JsonObject jsonObj = gson.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO
            Iterable<JsonElement> rewardsLog = jsonObj.getAsJsonArray("rewardsLog");
            if (rewardsLog == null) rewardsLog = Collections.emptyList();

            for (JsonElement item : rewardsLog) {
                JsonObject obj = item.getAsJsonObject();
                if (!obj.has("lootId")) continue;

                String key = obj.getAsJsonPrimitive("lootId").getAsString();
                int amount = obj.getAsJsonPrimitive("amount").getAsInt();

                this.collected.compute(key, (k, v) -> (v == null ? 0 : v) + amount);
                this.lastCollected.compute(key, (k, v) -> (v == null ? 0 : v) + amount);
            }
        }
        System.out.println("DispatchManager: " + type + " (" + id + ") " + (failed ? "failed" : "succeeded") + ": " + (failed ? response : ""));
        update(0);
        return !failed;
    }

    public List<String> collectAll() {
        List<InProgress> toCollect = data.getInProgress().values().stream()
                .filter(ip -> !ip.getCollectable().equals("0")).collect(Collectors.toList());
        return toCollect.stream().filter(this::collect).map(InProgress::getId).collect(Collectors.toList());
    }

    private enum InfoReader {
        PERMIT("name=\"permit\" value=\"([0-9]+)\"", DispatchData::setPermit),
        PERMIT_PLUS("name=\"permit plus\" value=\"([0-9]+)\"", DispatchData::setPermitPlus),
        GATE_UNIT("name=\"ggeu\" value=\"([0-9]+)\"", DispatchData::setGateUnits),
        SLOTS(":([0-9]+).*class=\"userCurrentMax\">([0-9]+)", DispatchData::setAvailableSlots, DispatchData::setMaxSlots),
        PRIME_COUPON("name=\"quickcoupon\" value=\"([0-9]+)\"", DispatchData::setPrimeCoupons),
        ITEMS("<tr class=\"dispatchItemRow([\\S\\s]+?)</tr>", DispatchData::parseRetrieverRow);

        private final Pattern regex;
        private final List<BiConsumer<DispatchData, String>> consumers;

        InfoReader(@Language("RegExp") String regex, List<BiConsumer<DispatchData, String>> consumers) {
            this.regex = Pattern.compile(regex);
            this.consumers = consumers;
        }

        @SafeVarargs
        InfoReader(@Language("RegExp") String regex, BiConsumer<DispatchData, String>... consumers) {
            this(regex, Arrays.asList(consumers));
        }

        @SafeVarargs
        InfoReader(@Language("RegExp") String regex, BiIntConsumer<DispatchData>... consumers) {
            this(regex, Arrays.stream(consumers)
                    .map(c -> (BiConsumer<DispatchData, String>) (d, s) -> c.accept(d, Integer.parseInt(s)))
                    .collect(Collectors.toList()));
        }

        public static boolean updateAll(String page, DispatchData data) {
            // Mark old in-progress for removal
            data.getInProgress().forEach((k, v) -> v.setForRemoval(true));
            // Mark old gate for removal
            data.getGates().forEach((k, v) -> v.setForRemoval(true));
            boolean updated = true;
            for (InfoReader reader : InfoReader.values()) {
                updated &= reader.update(page, data);
            }
            // Remove them if they have not gotten an update (they are collected already)
            data.getInProgress().values().removeIf(InProgress::getForRemoval);
            data.getGates().values().removeIf(Gate::getForRemoval);
            return updated;
        }

        private boolean update(String page, DispatchData data) {
            Matcher m = regex.matcher(page);
            if (!m.find()) return false;

            do {
                int max = Math.min(m.groupCount(), consumers.size());
                for (int i = 0; i < max; i++)
                    consumers.get(i).accept(data, m.group(i + 1));
            } while (m.find());

            return m.groupCount() == consumers.size();
        }
    }
}
