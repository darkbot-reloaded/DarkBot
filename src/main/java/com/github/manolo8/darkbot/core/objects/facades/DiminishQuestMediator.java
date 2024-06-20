package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.gui.DiminishQuestGui;
import eu.darkbot.api.API;
import eu.darkbot.api.managers.QuestAPI;

import static com.github.manolo8.darkbot.Main.API;

public class DiminishQuestMediator extends Updatable implements API.Singleton {
    private State state = State.NOT_EXIST;
    private final DiminishQuestGui diminishQuestGui;
    private final QuestProxy.Quest diminishQuest = new QuestProxy.Quest();
    private static final State[] STATES = State.values();

    public DiminishQuestMediator(DiminishQuestGui diminishQuestGui) {
        this.diminishQuestGui = diminishQuestGui;
    }

    @Override
    public void update() {
        if (!isValid()) return;
        int stateIndex = API.readInt(address, 0x50, 0x40) + 1;
        if (stateIndex >= 0 && stateIndex < STATES.length) {
            state = STATES[stateIndex];
            diminishQuest.updateIfChanged(API.readAtom(address, 0x50, 0x60));
            diminishQuest.update();
        } else {
            state = State.NOT_EXIST;
        }
    }

    public QuestAPI.Quest getDiminishQuest() {
        return diminishQuest;
    }

    public boolean isWaitAccepting() {
        return isValid() && State.WAIT_ACCEPTING.equals(state);
    }

    public boolean isAccepted() {
        return isValid() && State.ACCEPTED.equals(state);
    }

    public void accept() {
        diminishQuestGui.accept();
    }

    private enum State {
        NOT_EXIST,
        WAIT_ACCEPTING,
        UNKNOWN,
        ACCEPTED,
        DONE,
    }
}
