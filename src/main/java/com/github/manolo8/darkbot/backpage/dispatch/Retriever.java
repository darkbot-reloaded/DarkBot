package com.github.manolo8.darkbot.backpage.dispatch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Retriever {
    protected String id, name, type, tier, costString;
    protected Cost cost;
    public class Cost{
        int credits;
        int uri;
        int permit;
    }
    private final Pattern DISPATCH_COST = Pattern.compile("(?:Credits x (\\d+)(?:\\s+&\\s+)?)?" +
            "(?:Uridium x (\\d+)(?:\\s+&\\s+)?)?" +
            "(?:Permit x (\\d+)(?:\\s+&\\s+)?)?", Pattern.DOTALL);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getCost() {
        return costString;
    }

    public void setCost(String costString) {
        this.costString = costString;
        Matcher m = DISPATCH_COST.matcher(costString);
        if (m.find()){
            cost = cost == null ? new Cost() : cost;
            cost.credits = m.group(1) == null ? 0 : Integer.parseInt(m.group(1));
            cost.uri = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
            cost.permit = m.group(3) == null ? 0 : Integer.parseInt(m.group(3));
        }

    }

    public int getCreditCost(){
        return cost.credits;
    }
    public int getUridiumCost(){
        return cost.uri;
    }
    public int getPermitCost(){
        return cost.permit;
    }
    @Override
    public String toString() {
        return "Retriever{" +
                "id=" + id +
                "name=" + name +
                "type=" + type +
                "tier=" + tier +
                "cost=" + cost +
                "}";
    }
}
