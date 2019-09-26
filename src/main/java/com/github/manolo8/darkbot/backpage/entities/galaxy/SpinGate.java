package com.github.manolo8.darkbot.backpage.entities.galaxy;

public enum SpinGate {
    ABG("alpha", 1),
    DELTA("delta", 4),
    EPSILON("epsilon", 5),
    ZETA("zeta", 6),
    KAPPA("kappa", 7),
    LAMBDA("lambda", 8),
    HADES("hades", 13),
    KUIPER("streuner", 19);

    private String name;
    private int id;

    SpinGate(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getParam() {
        return "&gateID=" + id + "&" + name + "=1";
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}