package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.dispatch.BiIntConsumer;
import com.github.manolo8.darkbot.backpage.dispatch.DispatchData;
import com.github.manolo8.darkbot.backpage.dispatch.InProgress;
import com.github.manolo8.darkbot.backpage.dispatch.Retriever;
import com.github.manolo8.darkbot.utils.http.Method;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.intellij.lang.annotations.Language;

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
    private final Main main;
    private final DispatchData data;
    private long lastDispatcherUpdate;
    private final Map<String, Integer> collected;
    private final Map<String, Integer> lastCollected;
    private final Gson g;

    DispatchManager(Main main) {
        this.main = main;
        this.data = new DispatchData();
        this.collected = new HashMap<>();
        this.lastCollected = new HashMap<>();
        this.g = new Gson();
    }

    public DispatchData getData() {
        return data;
    }

    public boolean update(int expiryTime) {
        try {
            if (System.currentTimeMillis() <= lastDispatcherUpdate + expiryTime) return false;
            String page = main.backpage.getConnection("indexInternal.es?action=internalDispatch", Method.GET).getContent();

            if (page == null || page.isEmpty()) return false;
            lastDispatcherUpdate = System.currentTimeMillis();
            return InfoReader.updateAll(page, data);
        } catch (Exception e) {
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
        if (data.getAvailableSlots() <= 0) return false;
        if (retriever.getPermitCost() > data.getPermit()) {
            return handleResponse("Cannot hire", retriever.getId(), "(ERROR) Not enough permits");
        }
        try {
            String response = main.backpage.getConnection("ajax/dispatch.php", Method.POST)
                    .setRawParam("command", "sendDispatch")
                    .setRawParam("dispatchId", retriever.getId())
                    .getContent();
            return handleResponse("Hired dispatcher", retriever.getId(), response);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception hiring dispatcher: " + e);
        }
        return false;
    }

    public boolean collectInstant(InProgress progress) {
        try {
            System.out.println("Collecting Instant: Slot " + progress.getSlotId());
            if (data.getPrimeCoupons() <= 0)
                return handleResponse("Cannot instant collect", progress.getId(),
                        "(ERROR) No Prime Coupon available for instant collection");
            String response = main.backpage.getConnection("ajax/dispatch.php", Method.POST)
                    .setRawParam("command", "instantComplete")
                    .setRawParam("dispatchId", progress.getId())
                    .setRawParam("dispatchRewardPackage", progress.getDispatchRewardPackage())
                    .setRawParam("slotId", progress.getSlotId())
                    .getContent();

            return handleResponse("Collected retriever", progress.getId(), response);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception collecting dispatcher: " + e);
        }
        return false;
    }


    public boolean collect(InProgress progress) {
        if (progress.getCollectable().equals("0")) return false;
        try {
            System.out.println("Collecting: Slot " + progress.getSlotId());
            String response = main.backpage.getConnection("ajax/dispatch.php", Method.POST)
                    .setRawParam("command", "collectDispatch")
                    .setRawParam("slot", progress.getSlotId())
                    .getContent();

            return handleResponse("Collected retriever", progress.getId(), response);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception collecting dispatcher: " + e);
        }
        return false;
    }

    public boolean handleResponse(String type, String id, String response) {
        boolean failed = response.contains("ERROR");
        if (!failed) {
            this.lastCollected.clear();
            JsonObject jsonObj = g.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO
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
        PRIME_COUPON("name=\"quickcoupon\" value=\"([0-9]+)\"",DispatchData::setPrimeCoupons),
        ITEMS("<tr class=\"dispatchItemRow([\\S\\s]+?)</tr>", DispatchData::parseRow);

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
            boolean updated = true;
            for (InfoReader reader : InfoReader.values()) {
                updated &= reader.update(page, data);
            }
            // Remove them if they have not gotten an update (they are collected already)
            data.getInProgress().values().removeIf(InProgress::getForRemoval);
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
