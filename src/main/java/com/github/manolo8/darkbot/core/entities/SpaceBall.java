package com.github.manolo8.darkbot.core.entities;

// SpaceBall is a ship but without HP
public class SpaceBall extends Ship implements eu.darkbot.api.game.entities.SpaceBall {

    public SpaceBall(int id, long address) {
        super(id, address);
    }
}
