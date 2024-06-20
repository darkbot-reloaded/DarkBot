package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import com.github.manolo8.darkbot.core.objects.swf.FlashMap;
import eu.darkbot.api.managers.QuestAPI;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

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
        this.currentQuest.update(readAtom(0x98, 0x28));

        this.questGiverOpen = readBoolean(0x40);
        if (!questGiverOpen) return;

        long questInfoGiverSelectedAddr = readAtom(0xA8);
        this.questGiverSelectedInfo.update(questInfoGiverSelectedAddr);

        long questGiverSelectedAddr = readAtom(0xB0);
        this.questGiverSelected.update(questGiverSelectedAddr);
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
    public static class QuestListItem extends Auto implements QuestAPI.QuestListItem {
        private int id;
        private int levelRequired;
        private boolean selected;
        private boolean completed;
        private boolean activable;
        private String title;
        private String type;

        @Override
        public void update() {
            if (address == 0) return;

            this.id = readInt(0x24);
            this.levelRequired = readInt(0x28);
            this.selected = readBoolean(0x34);
            this.completed = readBoolean(0x38);
            this.activable = readBoolean(0x3C);
            this.title = readString(0x58);
            this.type = readString(0x70);
        }
    }

    @Getter
    public static class Quest extends Auto implements QuestAPI.Quest {
        private int id;
        private boolean active;
        private boolean completed;
        private String title;
        private String description;
        private final FlashList<QuestProxy.Requirement> requirements = FlashList.ofArray(Requirement::new);
        private final FlashList<QuestProxy.Reward> rewards = FlashList.ofArray(Reward::new);

        @Override
        public void update() {
            if (address == 0) return;

            this.id = readInt(0x20);
            this.active = readBoolean(0x24);
            this.completed = readBoolean(0x38, 0x38);
            this.title = readString(0x68);
            this.description = readString(0x70);
            this.requirements.update(readAtom(0x40));

            if (!this.rewards.isEmpty()) {
                return;
            }

            rewards.update(readAtom(0x50));
        }
    }

    @Getter
    @ToString
    public static class Reward extends Auto implements QuestAPI.Reward {
        private int amount;
        private String type;
        private boolean readed;

        @Override
        public void update() {
            if (address == 0 || readed) return;

            this.amount = readInt(0x20);
            this.type = readString(0x28);
            this.readed = true;
        }
    }

    @Getter
    @ToString
    public static class Requirement extends Auto implements QuestAPI.Requirement {
        private String description;
        private double progress;
        private double goal;
        private boolean completed;
        private boolean enabled;

        private final FlashList<QuestProxy.Requirement> requirements = FlashList.ofArray(Requirement::new);

        private String type;

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.enabled = readBoolean(0x24);
            this.completed = readBoolean(0x34);
            this.description = readString(0x60);
            this.progress = readDouble(0x78);
            this.goal = readDouble(0x80);

            long definitionAddr = readAtom(0x58);
            this.type = API.readString(definitionAddr, 0x28);

            requirements.update(readAtom(0x48));
        }
    }
}