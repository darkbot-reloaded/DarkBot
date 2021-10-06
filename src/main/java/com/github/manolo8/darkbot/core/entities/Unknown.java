package com.github.manolo8.darkbot.core.entities;

public class Unknown extends Entity {

    public String assetId;

    public Unknown(int id, long address, String assetId) {
        super(id, address);
        this.assetId = assetId;
    }

}
