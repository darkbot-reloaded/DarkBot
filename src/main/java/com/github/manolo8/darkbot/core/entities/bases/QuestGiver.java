package com.github.manolo8.darkbot.core.entities.bases;


import eu.darkbot.api.entities.Station;

public class QuestGiver extends BaseSpot implements Station.QuestGiver {
    public QuestGiver(int id, long address) {
        super(id, address);
    }
}
