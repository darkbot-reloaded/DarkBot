package com.github.manolo8.darkbot.backpage.dispatch;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatchData {
    private int permit, gateUnits, slots, maxSlots;

    private final RetrieverBuilder retrieverBuilder = new RetrieverBuilder();
    private final Map<String, Retriever> retrievers = new LinkedHashMap<>();
    private final Map<String, InProgress> progressSlots = new LinkedHashMap<>();

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

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
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

    public void parseRetriever(String str) {
        retrieverBuilder.build(str);
        retrieverBuilder.progress(str);
    }

    public Map<String, InProgress> getInProgress() { return progressSlots; }

    public class RetrieverBuilder {
        private final Pattern PATTERN = Pattern.compile("dispatchId=\"(.+?)\".*?" +
                "dispatch_item_name_col\">\\s+(.+?)\\s+<.*?" +
                "dispatch_item_type\">\\s+(.+?)\\s+<.*?" +
                "dispatch_item_tier\">\\s+(.+?)\\s+<.*?" +
                "dispatch_item_cost\">\\s+(.+?)\\s+<", Pattern.DOTALL);
        private final Pattern progressPattern = Pattern.compile("collectable=\"(.+?)\".*?" +
                "dispatchId=\"(.+?)\".*?" +
                "dispatchRewardPackage=\"(.+?)\".*?" +
                "slotId=\"(.+?)\".*?"+
                "dispatch_item_name_col\">\\s+(.+?)\\s+<.*?",Pattern.DOTALL);

        public void build(String string) {
            if (string == null || string.isEmpty()) return;
            if (!string.contains("dispatch_item_name_col") ||
                    !string.contains("dispatch_item_type") ||
                    !string.contains("dispatch_item_tier") ||
                    !string.contains("dispatch_item_cost")) return;
            Matcher m = PATTERN.matcher(string);
            if (!m.find()) return;

            String id = m.group(1);
            Retriever r = retrievers.get(id);
            if (r == null) retrievers.put(id, r = new Retriever());

            r.setId(id);
            r.setName(m.group(2));
            r.setType(m.group(3));
            r.setTier(m.group(4));
            r.setCost(m.group(5));
        }

        public void progress(String string){
            if (string == null || string.isEmpty()) return;
            if (!string.contains("dispatchRewardPackage") ||
                    !string.contains("slotId") ||
                    !string.contains("dispatch_item_name_col")) return;
            Matcher m = progressPattern.matcher(string);
            if (!m.find()) return;

            String slotID = m.group(4);
            InProgress r = progressSlots.get(slotID);
            if (r == null) progressSlots.put(slotID, r = new InProgress());

            r.setCollectable(m.group(1));
            r.setId(m.group(2));
            r.setSlotID(slotID);
            r.setName(m.group(5));

        }
    }
    @Override
    public String toString(){
        return "DisptachData{" +
                "permit=" + permit +
                "gateUnits=" + gateUnits +
                "slots=" + slots +
                "maxSlots=" + maxSlots +
                "cost=" + retrieverBuilder.toString() +
                "}"
                ;
    }

}
