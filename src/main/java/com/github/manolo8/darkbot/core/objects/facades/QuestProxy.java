package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import com.github.manolo8.darkbot.core.objects.swf.FlashMap;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.managers.QuestAPI;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

        this.selectedTab = API.readMemoryInt(address, 0x50);
        this.currentQuest.update(API.readMemoryPtr(address, 0x98, 0x28));

        this.questGiverOpen = API.readMemoryBoolean(address, 0x40);
        if (!questGiverOpen) return;

        long questInfoGiverSelectedAddr = API.readMemoryPtr(address + 0xA8);
        this.questGiverSelectedInfo.update(questInfoGiverSelectedAddr);

        long questGiverSelectedAddr = API.readMemoryPtr(address + 0xB0);
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
            questGiverItemsMap.update(API.readMemoryPtr(address + 0x58));
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

            this.id = API.readMemoryInt(address + 0x24);
            this.levelRequired = API.readMemoryInt(address + 0x28);
            this.selected = API.readMemoryBoolean(address + 0x34);
            this.completed = API.readMemoryBoolean(address + 0x38);
            this.activable = API.readMemoryBoolean(address + 0x3C);
            this.title = API.readMemoryString(address, 0x58);
            this.type = API.readMemoryString(address, 0x70);
        }
    }

    @Getter
    public static class Quest extends Auto implements QuestAPI.Quest {
        private int id;
        private boolean active;
        private boolean completed;
        private String title;
        private String description;
        private final FlashList<QuestProxy.Requirement> requirements = FlashList.ofArray(Requirement.class);

        @Getter(AccessLevel.NONE)
        private final ObjArray rewardItemsArr = ObjArray.ofArrObj(true);
        private final List<QuestProxy.Reward> rewards = new ArrayList<>();

        @Override
        public void update() {
            if (address == 0) return;

            this.id = API.readMemoryInt(address + 0x20);
            this.active = API.readMemoryBoolean(address + 0x24);
            this.completed = API.readMemoryBoolean(address, 0x38, 0x38);
            this.title = API.readMemoryString(address, 0x68);
            this.description = API.readMemoryString(address, 0x70);
            this.requirements.update(API.readMemoryPtr(address + 0x40));

            if (!this.rewards.isEmpty()) {
                return;
            }

            rewardItemsArr.update(API.readMemoryPtr(address + 0x50));
            rewardItemsArr.sync(rewards, Reward::new);
        }
    }

    @Getter
    @ToString
    public static class Reward extends Auto implements QuestAPI.Reward {
        private int amount;
        private String type;

        @Override
        public void update() {
            if (address == 0) return;

            this.amount = API.readMemoryInt(address + 0x20);
            this.type = API.readMemoryString(address, 0x28);
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

        @Getter(AccessLevel.NONE)
        private final ObjArray requirementItemsArr = ObjArray.ofArrObj(true);
        private final List<QuestProxy.Requirement> requirements = new ArrayList<>();

        private String type;

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.enabled = API.readMemoryBoolean(address + 0x24);
            this.completed = API.readMemoryBoolean(address, 0x34);
            this.description = API.readMemoryString(address, 0x60);
            this.progress = API.readMemoryDouble(address + 0x78);
            this.goal = API.readMemoryDouble(address + 0x80);

            long definitionAddr = API.readMemoryPtr(address + 0x58);
            this.type = API.readMemoryString(definitionAddr, 0x28);

            requirementItemsArr.update(API.readMemoryPtr(address + 0x48));
            requirementItemsArr.sync(requirements, Requirement::new);
        }
    }
}