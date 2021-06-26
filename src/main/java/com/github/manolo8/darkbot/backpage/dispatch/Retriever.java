package com.github.manolo8.darkbot.backpage.dispatch;

public class Retriever {
    protected String id, name, type, tier, cost;

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
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    @Override
    public String toString(){
        return "Retriever{" +
                "id=" + id +
                "name=" + name +
                "type=" + type +
                "tier=" + tier +
                "cost=" + cost +
                "}"
                ;
    }
}
