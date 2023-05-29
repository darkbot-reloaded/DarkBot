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

@Deprecated
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
        return false;
    }

    /**
     * @param expiryTime only update if within
     * @return null if update wasn't required (non-expired), true if updated ok, false if update failed
     */
    public Boolean update(long expiryTime) {
        return false;
    }

    public Map<String, Integer> getCollected() {
        return collected;
    }

    public Map<String, Integer> getLastCollected() {
        return lastCollected;
    }

    public boolean hireRetriever(Retriever retriever) {
        return false;
    }

    public boolean collectInstant(InProgress progress) {
        return false;
    }

    public boolean hireGate(Gate gate) {
        return false;
    }

    public boolean collectGate(Gate gate) {
        return false;
    }


    public boolean collect(InProgress progress) {
        return false;
    }

    private boolean handleResponse(String type, String id, String response) {
        return false;
    }

    public List<String> collectAll() {
        return Collections.emptyList();
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
