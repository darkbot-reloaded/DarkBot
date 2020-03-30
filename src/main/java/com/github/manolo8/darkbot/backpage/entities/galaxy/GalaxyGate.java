package com.github.manolo8.darkbot.backpage.entities.galaxy;

public enum GalaxyGate {
    ALPHA  ("alpha",    1),
    BETA   ("beta",     2),
    GAMMA  ("gamma",    3),
    DELTA  ("delta",    4),
    EPSILON("epsilon",  5),
    ZETA   ("zeta",     6),
    KAPPA  ("kappa",    7),
    LAMBDA ("lambda",   8),
    // Just to get infos about Kronos
    KRONOS ("kronos",   12),
    HADES  ("hades",    13),
    KUIPER ("streuner", 19);

    private String name;
    private int id;

    GalaxyGate(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getParam() {
        return "&gateID=" + getId() + "&" + getName() + "=1";
    }

    public String getIdParam() {
        return "&gateID=" + getId();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    boolean match(Object o) {
       if (o instanceof Integer) return o.equals(getId());
       if (o instanceof String)  return o.equals(getName());
       return false;
    }
}