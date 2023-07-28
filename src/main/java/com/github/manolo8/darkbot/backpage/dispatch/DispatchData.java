package com.github.manolo8.darkbot.backpage.dispatch;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatchData {
    private final DataBuilder dataBuilder = new DataBuilder();
    private final Map<String, Retriever> retrievers = new LinkedHashMap<>();
    private final Map<String, InProgress> progressSlots = new LinkedHashMap<>();
    private final Map<String, Gate> gates = new LinkedHashMap<>();
    private int permit = 0, permitPlus = 0, gateUnits = 0, availableSlots = 0, maxSlots = 0, primeCoupons = 0;

    public int getPermit() {
        return permit;
    }

    public void setPermit(int permit) {
        this.permit = permit;
    }

    public void setPermitPlus(int permitPlus) {
        this.permitPlus = permitPlus;
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

    public int getPrimeCoupons() {
        return primeCoupons;
    }

    public void setPrimeCoupons(int primeCoupons) {
        this.primeCoupons = primeCoupons;
    }

    public Map<String, Retriever> getRetrievers() {
        return retrievers;
    }

    public Map<String, InProgress> getInProgress() {
        return progressSlots;
    }

    public Map<String, Gate> getGates() {
        return gates;
    }

    public void parseRetrieverRow(String str) {
        if (dataBuilder.buildRetriever(str)) return;
        if (dataBuilder.buildInProgress(str)) return;
        if (dataBuilder.buildGate(str)) return;
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
                "dispatch_item_name_col\"> ?(.+?) ?<.*?" +
                "dispatch_item_type\"> ?(.+?) ?<.*?" +
                "dispatch_item_tier\"> ?(.+?) ?<.*?" +
                "dispatch_item_cost\"> ?(?:(.+?) ?<br>)? ?(.+?) ?<br>", Pattern.DOTALL);
        private final Pattern PROGRESS_PATTERN = Pattern.compile("collectable=\"(\\d+)\".*?" +
                "dispatchId=\"(.+?)\".*?" +
                "dispatchRewardPackage=\"(.+?)\".*?" +
                "slotId=\"(\\d+)\".*?" +
                "dispatch_item_name_col\"> ?(.+?) ?<", Pattern.DOTALL);
        private final Pattern GATE_PATTERN = Pattern.compile("gateId=\"(.+?)\".*?" +
                " (collectable=\"(\\d+)\")?.*?" +
                "dispatch_item_name_col\"> ?(.+?) ?<.*?" +
                "dispatch_remaining_time_col\"> ?(.+?) ?<input.*?" +
                "dispatch_item_cost\">(.+?)?<",Pattern.DOTALL);
        private final Pattern DISPATCH_COST = Pattern.compile("(\\d+)", Pattern.DOTALL);

        public boolean buildRetriever(String string) {
            if (string == null || string.isEmpty()) return false;
            if (!string.contains("dispatch_item_name_col") ||
                    !string.contains("dispatch_item_type") ||
                    !string.contains("dispatch_item_tier") ||
                    !string.contains("dispatch_item_cost")) return false;
            Matcher m = RETRIEVER_PATTERN.matcher(string.replaceAll("\\s+", " "));
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
                r.setCreditCost(0);
                Matcher n = DISPATCH_COST.matcher(m.group(5));
                if (n.find()) r.setUridiumCost(Integer.parseInt(n.group(1)));

                n = DISPATCH_COST.matcher(m.group(6));
                if (n.find()) r.setPermitCost(Integer.parseInt(n.group(1)));
            } else {
                r.setCost(m.group(6));
                Matcher n = DISPATCH_COST.matcher(m.group(6));
                if (n.find()) r.setCreditCost(Integer.parseInt(n.group(1)));

                r.setUridiumCost(0);
                r.setPermitCost(0);
            }
            return true;
        }

        public boolean buildInProgress(String string) {
            if (string == null || string.isEmpty()) return false;
            if (!string.contains("dispatchRewardPackage") ||
                    !string.contains("slotId") ||
                    !string.contains("dispatch_item_name_col")) return false;
            Matcher m = PROGRESS_PATTERN.matcher(string.replaceAll("\\s+", " "));
            if (!m.find()) return false;

            String slotID = m.group(4);
            InProgress r = progressSlots.get(slotID);
            if (r == null) progressSlots.put(slotID, r = new InProgress());

            r.setCollectable(m.group(1));
            r.setDispatchRewardPackage(m.group(3));
            r.setId(m.group(2));
            r.setSlotId(slotID);
            r.setName(m.group(5));
            r.setForRemoval(false);
            return true;
        }

        public boolean buildGate(String string) {
            if (string == null || string.isEmpty()) return false;
            if (!string.contains("gateId")) return false;
            Matcher m = GATE_PATTERN.matcher(string.replaceAll("\\s+", " "));
            if (!m.find()) return false;

            String gateId = m.group(1);
            Gate gate = gates.get(gateId);
            if (gate == null) gates.put(gateId, gate = new Gate());

            gate.setId(gateId);
            gate.setCollectable(m.group(3) != null ? m.group(3) : "0");
            gate.setName(m.group(4));
            gate.setTime(m.group(5).length() < 10 ? m.group(5) : "0");
            gate.setCost(m.group(6).length() > 5 ? m.group(6) : "0");

            if (gate.getCollectable().equalsIgnoreCase("1") || (gate.getTime().equalsIgnoreCase("0") && gate.getCost().equalsIgnoreCase("0"))) {
                gate.setInProgress(true);
            }
            gate.setForRemoval(false);
            return true;
        }
    }

}
