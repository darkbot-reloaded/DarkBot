package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.core.utils.Lazy;

import java.util.HashMap;

public class Config {

    public int WORKING_MAP = 26;

    public int OFFENSIVE_CONFIG = 1;
    public char OFFENSIVE_FORMATION = '8';

    public int RUN_CONFIG = 2;
    public char RUN_FORMATION = '9';

    public long REFRESH_TIME = 60 * 60 * 1000;

    public int CURRENT_MODULE;

    public int MAX_DEATHS = 10;

    public int REPAIR_LOCAL = 0;

    public double REPAIR_HP;
    public double WAIT_HP;

    //ENTITIES
    public HashMap<String, BoxInfo> boxInfos = new HashMap<>();
    public HashMap<String, NpcInfo> npcInfos = new HashMap<>();
    //ENTITIES

    //LOOT MODULE
    public boolean RUN_FROM_ENEMIES;
    public boolean RUN_FROM_ENEMIES_IN_SIGHT;
    public char AMMO_KEY = '3';
    public boolean AUTO_SAB = true;
    public char AUTO_SAB_KEY = '4';
    //LOOT MODULE

    //COLLECTOR MODULE
    public boolean STAY_AWAY_FROM_ENEMIES;
    public boolean AUTO_CLOACK;
    public char AUTO_CLOACK_KEY;
    //COLLECTOR MODULE

    public transient boolean changed;

    public transient Lazy<String> addedBox = new Lazy<>();
    public transient Lazy<String> addedNpc = new Lazy<>();
}
