package com.github.manolo8.darkbot.backpage.entities;

import com.google.gson.annotations.SerializedName;

public class Item {
    @SerializedName("L")
    private int lootId;

    @SerializedName("EQC")
    private String equipedConfi;

    @SerializedName("I")
    private String itemId;

    @SerializedName("EQH")
    private String equipedHangar;

    @SerializedName("EQT")
    private String equipedTarget;

    @SerializedName("LV")
    private int level;

    @SerializedName("CH")
    private String loads;

    @SerializedName("Q")
    private int quantity;
    
    @SerializedName("SL")
    private int shieldUpgradeLevel;
    
    @SerializedName("DL")
    private int damageUpgradeLevel;

    public Item(int lootId, String equipedConfi, String itemId, String equipedHangar, String equipedTarget, int level, String loads, int quantity, int shieldUpgradeLevel, int damageUpgradeLevel) {
        this.lootId = lootId;
        this.equipedConfi = equipedConfi;
        this.itemId = itemId;
        this.equipedHangar = equipedHangar;
        this.equipedTarget = equipedTarget;
        this.level = level;
        this.loads = loads;
        this.quantity = quantity;
        this.shieldUpgradeLevel = shieldUpgradeLevel;
        this.damageUpgradeLevel = damageUpgradeLevel;
    }

    public int getLoot() {
        return this.lootId;
    }

    public String getEquipedConfi() {
        return this.equipedConfi;
    }

    public String getItemId() {
        return this.itemId;
    }

    public String getEquipedHangar() {
        return this.equipedHangar;
    }

    public String getEquipedTarget() {
        return this.equipedTarget;
    }

    public int getLevel() {
        return this.level;
    }

    public String getLoads() {
        return this.loads;
    }

    public int getQuantity() {
        return this.quantity;
    }
    
    public int getShieldUpgradeLevel() {
        return this.shieldUpgradeLevel;
    }
    
    public int getDamageUpgradeLevel() {
        return this.damageUpgradeLevel;
    }
}
