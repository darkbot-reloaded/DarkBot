package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import com.github.manolo8.darkbot.core.objects.swf.FlashMap;
import eu.darkbot.api.managers.QuestAPI;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QuestProxy extends Updatable implements QuestAPI {
    private boolean questsUpdated = false;

    // Quest window
    @Getter
    private int selectedTab;
    private final QuestProxy.Quest currentQuest = new Quest();

    // Within quest giver
    private final QuestProxy.Quest questGiverSelected = new Quest();
    private final QuestProxy.QuestListItem questGiverSelectedInfo = new QuestListItem();

    private final FlashMap<Integer, QuestProxy.QuestListItem> questGiverItemsMap = FlashMap.of(Integer.class, QuestProxy.QuestListItem.class);

    @Getter
    private boolean questGiverOpen;

    @Override
    public void update() {
        if (address == 0) return;

        this.questsUpdated = false;

        this.selectedTab = readInt(0x50);
        this.currentQuest.updateIfChanged(readAtom(0x98, 0x28));
        this.currentQuest.update();

        this.questGiverOpen = readBoolean(0x40);
        if (!questGiverOpen) return;

        this.questGiverSelectedInfo.updateIfChanged(readAtom(0xA8));
        this.questGiverSelectedInfo.update();

        long questGiverSelectedAddr = readAtom(0xB0);
        this.questGiverSelected.updateIfChanged(questGiverSelectedAddr);
        this.questGiverSelected.update();
    }

    @Override
    public @Nullable QuestAPI.Quest getDisplayedQuest() {
        return currentQuest.address == 0 ? null : currentQuest;
    }

    @Override
    public @Nullable QuestAPI.Quest getSelectedQuest() {
        return !this.questGiverOpen || questGiverSelected.address == 0 ? null : questGiverSelected;
    }

    @Override
    public @Nullable QuestAPI.QuestListItem getSelectedQuestInfo() {
        return !this.questGiverOpen || questGiverSelectedInfo.address == 0 ? null : questGiverSelectedInfo;
    }

    @Override
    public @Nullable List<? extends QuestAPI.QuestListItem> getCurrestQuests() {
        if (!questsUpdated) {
            questsUpdated = true;
            questGiverItemsMap.update(readAtom(0x58));
        }
        return questGiverItemsMap.getValueList();
    }

    @Getter
    @ToString
    public static class QuestListItem extends Updatable implements QuestAPI.QuestListItem {
        private int id;
        private int levelRequired;
        private boolean selected;
        private boolean completed;
        private boolean activable;
        private String title;
        private String type;

        @Override
        public void update(long address) {
            super.update(address);

            this.id = readInt(0x24);
            this.levelRequired = readInt(0x28);
            this.title = readString(0x58);
            this.type = readString(0x70);

        }

        @Override
        public void update() {
            if (address == 0) return;

            this.selected = readBoolean(0x34);
            this.completed = readBoolean(0x38);
            this.activable = readBoolean(0x3C);
        }
    }

    @Getter
    public static class Quest extends Updatable implements QuestAPI.Quest {
        private int id;
        private boolean active;
        private boolean completed;
        private String title;
        private String description;
        private final FlashList<QuestProxy.Requirement> requirements = FlashList.ofArray(Requirement::new);
        private final FlashList<QuestProxy.Reward> rewards = FlashList.ofArray(Reward::new);

        @Override
        public void update(long address) {
            super.update(address);

            this.id = readInt(0x20);
            this.title = readString(0x68);
            this.description = readString(0x70);
            this.requirements.update(readAtom(0x40));
            this.rewards.update(readAtom(0x50));
        }

        @Override
        public void update() {
            if (address == 0) return;

            this.active = readBoolean(0x24);
            this.completed = readBoolean(0x38, 0x38);
            this.requirements.update();
        }
    }

    @Getter
    @ToString
    public static class Reward extends Updatable implements QuestAPI.Reward {
        private int amount;
        private String type;

        @Override
        public void update(long address) {
            super.update(address);

            this.amount = readInt(0x20);
            this.type = readString(0x28);
        }

        @Override
        public void update() {
            // no-op, rewards are immutable
        }
    }

    @Getter
    @ToString
    public static class Requirement extends Updatable implements QuestAPI.Requirement {
        private final FlashList<QuestProxy.Requirement> requirements = FlashList.ofArray(Requirement::new);

        private String type;
        private String description;
        private double progress;
        private double goal;
        private boolean completed;
        private boolean enabled;

        @Override
        public void update(long address) {
            super.update(address);

            this.requirements.update(readAtom(0x48));
            this.type = readString(0x58, 0x28);
            this.description = readString(0x60);
        }

        @Override
        public void update() {
            if (address == 0) return;

            this.requirements.update();

            this.enabled = readBoolean(0x24);
            this.completed = readBoolean(0x34);
            this.progress = readDouble(0x78);
            this.goal = readDouble(0x80);
        }
    }
}