package com.github.manolo8.darkbot.backpage.dispatch;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatchData {
    private final DataBuilder dataBuilder = new DataBuilder();
    private final Map<String, Retriever> retrievers = new LinkedHashMap<>();
    private final Map<String, InProgress> progressSlots = new LinkedHashMap<>();
    private int permit, gateUnits, availableSlots, maxSlots;

    public int getPermit() {
        return permit;
    }

    public void setPermit(int permit) {
        this.permit = permit;
    }

    public int getGateUnits() {
        return gateUnits;
    }

    public void setGateUnits(int gateUnits) {
        this.gateUnits = gateUnits;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }

    public int getMaxSlots() {
        return maxSlots;
    }

    public void setMaxSlots(int maxSlots) {
        this.maxSlots = maxSlots;
    }

    public Map<String, Retriever> getRetrievers() {
        return retrievers;
    }

    public Map<String, InProgress> getInProgress() {
        return progressSlots;
    }

    public void parseRow(String str) {
        if (dataBuilder.buildRetriever(str)) return;
        if (dataBuilder.buildInProgress(str)) return;
    }

    @Override
    public String toString() {
        return "DisptachData{" +
                "permit=" + permit +
                "gateUnits=" + gateUnits +
                "availableSlots=" + availableSlots +
                "maxSlots=" + maxSlots +
                "}";
    }

    public class DataBuilder {
        private final Pattern RETRIEVER_PATTERN = Pattern.compile("dispatchId=\"(.+?)\".*?" +
                "dispatch_item_name_col\">\\s+(.+?)\\s+<.*?" +
                "dispatch_item_type\">\\s+(.+?)\\s+<.*?" +
                "dispatch_item_tier\">\\s+(.+?)\\s+<.*?" +
                "dispatch_item_cost\">(?:\\s+(.+?)\\s+<br>)?(?:\\s+(.+?)\\s+<br>)", Pattern.DOTALL);
        private final Pattern PROGRESS_PATTERN = Pattern.compile("collectable=\"(.+?)\".*?" +
                "dispatchId=\"(.+?)\".*?" +
                "slotId=\"(.+?)\".*?" +
                "dispatch_item_name_col\">\\s+(.+?)\\s+<.*?", Pattern.DOTALL);
        private final Pattern DISPATCH_COST = Pattern.compile("(\\d+)", Pattern.DOTALL);

        public boolean buildRetriever(String string) {
            if (string == null || string.isEmpty()) return false;
            if (!string.contains("dispatch_item_name_col") ||
                    !string.contains("dispatch_item_type") ||
                    !string.contains("dispatch_item_tier") ||
                    !string.contains("dispatch_item_cost")) return false;
            Matcher m = RETRIEVER_PATTERN.matcher(string);
            if (!m.find()) return false;

            String id = m.group(1);
            Retriever r = retrievers.get(id);
            if (r == null) retrievers.put(id, r = new Retriever());

            r.setId(id);
            r.setName(m.group(2));
            r.setType(m.group(3));
            r.setTier(m.group(4));
            if (m.group(5) != null) {
                r.setCost(m.group(5) + " & " + m.group(6));
                Matcher n = DISPATCH_COST.matcher(m.group(5));
                if (n.find()) r.setUridiumCost(Integer.parseInt(n.group(1)));

                n = DISPATCH_COST.matcher(m.group(6));
                if (n.find()) r.setPermitCost(Integer.parseInt(n.group(1)));
            } else {
                r.setCost(m.group(6));
                Matcher n = DISPATCH_COST.matcher(m.group(6));
                if (n.find()) r.setCreditCost(Integer.parseInt(n.group(1)));
            }
            return true;
        }

        public boolean buildInProgress(String string) {
            if (string == null || string.isEmpty()) return false;
            if (!string.contains("dispatchRewardPackage") ||
                    !string.contains("slotId") ||
                    !string.contains("dispatch_item_name_col")) return false;
            Matcher m = PROGRESS_PATTERN.matcher(string);
            if (!m.find()) return false;

            String slotID = m.group(3);
            InProgress r = progressSlots.get(slotID);
            if (r == null) progressSlots.put(slotID, r = new InProgress());

            r.setCollectable(m.group(1));
            r.setId(m.group(2));
            r.setSlotId(slotID);
            r.setName(m.group(4));
            r.setForRemoval(false);
            return true;
        }
    }

}
