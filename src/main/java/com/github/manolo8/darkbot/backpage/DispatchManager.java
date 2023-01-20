package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.backpage.dispatch.BiIntConsumer;
import com.github.manolo8.darkbot.backpage.dispatch.DispatchData;
import com.github.manolo8.darkbot.backpage.dispatch.InProgress;
import com.github.manolo8.darkbot.backpage.dispatch.Retriever;
import com.github.manolo8.darkbot.backpage.dispatch.Gate;
import com.github.manolo8.darkbot.utils.CaptchaAPI;
import com.github.manolo8.darkbot.utils.http.Http;
import com.github.manolo8.darkbot.utils.http.Method;
import com.github.manolo8.darkbot.utils.login.LoginUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.intellij.lang.annotations.Language;

import java.net.URL;
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
    private long lastDispatcherUpdate;
    private final Map<String, Integer> collected;
    private final Map<String, Integer> lastCollected;
    private final Gson gson;
    private final CaptchaAPI captchaAPI;
    private String requestID = "";

    DispatchManager(BackpageManager backpageManager) {
        this.backpageManager = backpageManager;
        this.data = new DispatchData();
        this.collected = new HashMap<>();
        this.lastCollected = new HashMap<>();
        this.gson = backpageManager.getGson();
        this.captchaAPI = backpageManager.getCaptchaSolver();
    }

    public DispatchData getData() {
        return data;
    }

    @Deprecated
    public boolean update(int expiryTime) {
        return this.update((long) expiryTime);
    }

    public boolean update(long expiryTime) {
        try {
            if (System.currentTimeMillis() <= lastDispatcherUpdate + expiryTime) return false;
            String page = "";
            if(requestID.isEmpty()) {
                page = backpageManager.getConnection("indexInternal.es?action=internalDispatch", Method.GET).getContent();
            }
            if(page.contains("id=\"captchaScriptContainer\"")){
                if (backpageManager.main.config.MISCELLANEOUS.SOLVE_BACKPAGE_CAPTCHA && captchaAPI != null) {
                    try {
                        if(requestID.isEmpty()) {
                            URL url = new URL(backpageManager.getInstanceURI().toString() + "indexInternal.es?action=lostPilot&desiredAction=dispatch");
                            requestID = captchaAPI.solveCaptchaRequestId(url, page);
                        }
                        if(captchaAPI.isCaptchaSolved(requestID)){
                            Map<String, String> extraPostParams = captchaAPI.fetchCaptchaResponse(requestID);
                            Http http = backpageManager.getConnection("ajax/lostpilot.php", Method.POST)
                                    .setParam("command", "checkReCaptcha")
                                    .setParam("desiredAction", "dispatch");
                            extraPostParams.forEach(http::setParam);
                            page = http.getContent();
                            requestID = "";
                        }
                    } catch (Throwable t) {
                        System.out.println("Captcha resolver failed to resolve dispatch captcha");
                        t.printStackTrace();
                    }
                } else {
                    System.out.println("Captcha Detected in Dispatch Page, Captcha resolver not enabled or No captcha resolver is configured");
                }
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
            String response = backpageManager.getConnection("ajax/dispatch.php", Method.POST)
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
            String response = backpageManager.getConnection("ajax/dispatch.php", Method.POST)
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

            String response = backpageManager.getConnection("ajax/dispatch.php", Method.POST)
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
            String response = backpageManager.getConnection("ajax/dispatch.php", Method.POST)
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
            String response = backpageManager.getConnection("ajax/dispatch.php", Method.POST)
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
        System.out.println(type + " (" + id + ") " + (failed ? "failed" : "succeeded") + ": " + (failed ? response : ""));
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
