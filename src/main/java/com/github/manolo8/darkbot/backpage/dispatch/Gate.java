package com.github.manolo8.darkbot.backpage.dispatch;

public class Gate {
    protected String id, name, time, cost, collectable;

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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getCollectable() {
        return collectable;
    }

    public void setCollectable(String collectable) {
        this.collectable = collectable;
    }

    @Override
    public String toString() {
        return "Retriever{" +
                "id=" + id +
                "name=" + name +
                "time=" + time +
                "cost=" + cost +
                "collectable=" + collectable +
                "}";
    }
}
