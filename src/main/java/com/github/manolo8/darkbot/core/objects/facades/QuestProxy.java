package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;

import eu.darkbot.api.managers.QuestAPI;

import static com.github.manolo8.darkbot.Main.API;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public class QuestProxy extends Updatable implements QuestAPI {
    private @Nullable QuestProxy.Quest currentQuest = new Quest();

    private final List<QuestProxy.QuestListItem> questItems = new ArrayList<>();
    private boolean questsUpdated = false;

    private final ObjArray questItemsArr = ObjArray.ofArrObj(true);

    private @Nullable QuestProxy.Quest questGiverSelected = new Quest();
    private @Nullable QuestProxy.QuestListItem questInfoGiverSelected = new QuestListItem();

    private boolean visibleQuestGiver;
    private int tabSelected;

    @Override
    public void update() {
        if (address == 0) {
            return;
        }

        this.questsUpdated = false;

        long questClass = API.readMemoryPtr(address + 0x98);

        long currentQuestAddr = API.readMemoryPtr(questClass + 0x28);
        this.currentQuest.update(currentQuestAddr);

        this.visibleQuestGiver = API.readMemoryBoolean(address, 0x40);

        if (!visibleQuestGiver) {
            return;
        }

        this.tabSelected = API.readMemoryInt(address, 0x50);

        long questInfoGiverSelectedAddr = API.readMemoryPtr(address + 0xA8);
        this.questInfoGiverSelected.update(questInfoGiverSelectedAddr);

        long questGiverSelectedAddr = API.readMemoryPtr(address + 0xB0);
        this.questGiverSelected.update(questGiverSelectedAddr);
    }

    @Override
    public @Nullable QuestAPI.Quest getDisplayedQuest() {
        return currentQuest.address == 0 ? null : currentQuest;
    }

    @Override
    public @Nullable QuestAPI.Quest getSelectedQuest() {
        return !this.visibleQuestGiver || questGiverSelected.address == 0 ? null
                : questGiverSelected;
    }

    @Override
    public @Nullable QuestAPI.QuestListItem getSelectedQuestInfo() {
        return !this.visibleQuestGiver || questInfoGiverSelected.address == 0 ? null
                : questInfoGiverSelected;
    }

    @Override
    public @Nullable List<? extends QuestAPI.QuestListItem> getCurrestQuests() {
        if (!questsUpdated) {
            questsUpdated = true;
            questItemsArr.update(API.readMemoryPtr(address + 0x58));
            questItemsArr.sync(questItems, QuestListItem::new);
        }

        return questItems;
    }

    @Override
    public boolean isQuestGiverOpen() {
        return visibleQuestGiver;
    }

    @Override
    public int getSelectedTab() {
        return tabSelected;
    }

    public static class QuestListItem extends Auto implements QuestAPI.QuestListItem {
        private int id;
        private boolean selected;
        private boolean completed;
        private String title;
        private String type;
        private int levelRequired;
        private boolean activable;

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.id = API.readMemoryInt(address + 0x24);
            this.levelRequired = API.readMemoryInt(address + 0x28);
            this.selected = API.readMemoryBoolean(address + 0x34);
            this.completed = API.readMemoryBoolean(address + 0x38);
            this.activable = API.readMemoryBoolean(address + 0x3C);
            this.title = API.readMemoryString(address, 0x58);
            this.type = API.readMemoryString(address, 0x70);
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public int getLevelRequired() {
            return levelRequired;
        }

        @Override
        public boolean isSelected() {
            return selected;
        }

        @Override
        public boolean isCompleted() {
            return completed;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public boolean isActivable() {
            return activable;
        }
    }

    public static class Quest extends Auto implements QuestAPI.Quest {
        private boolean active;
        private String description;
        private String title;
        private int id;
        private boolean completed;

        private final FlashList<QuestProxy.Requirement> requirementItems = FlashList.ofArray(Requirement.class);

        private final List<QuestProxy.Reward> rewardItems = new ArrayList<>();
        private final ObjArray rewardItemsArr = ObjArray.ofArrObj(true);

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.id = API.readMemoryInt(address + 0x20);
            this.active = API.readMemoryBoolean(address + 0x24);
            this.title = API.readMemoryString(address, 0x68);
            this.description = API.readMemoryString(address, 0x70);

            long requirementAddr = API.readMemoryPtr(address + 0x38);
            this.completed = API.readMemoryBoolean(requirementAddr, 0x38);

            requirementItems.update(API.readMemoryPtr(address + 0x40));
            requirementItems.update();

            if (this.rewardItems.size() > 0) {
                return;
            }

            rewardItemsArr.update(API.readMemoryPtr(address + 0x50));
            rewardItemsArr.sync(rewardItems, Reward::new);
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public boolean isCompleted() {
            return completed;
        }

        @Override
        public List<? extends QuestAPI.Requirement> getRequirements() {
            return requirementItems;
        }

        @Override
        public List<? extends QuestAPI.Reward> getRewards() {
            return rewardItems;
        }
    }

    public static class Reward extends Auto implements QuestAPI.Reward {
        private int amount;
        private String type;

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.amount = API.readMemoryInt(address + 0x20);
            this.type = API.readMemoryString(address, 0x28);
        }

        @Override
        public int getAmount() {
            return amount;
        }

        @Override
        public String getType() {
            return type;
        }
    }

    public static class Requirement extends Auto implements QuestAPI.Requirement {
        private String description;
        private double goalReached;
        private double goal;
        private boolean completed;
        private boolean enabled;

        private final List<QuestProxy.Requirement> requirementItems = new ArrayList<>();
        private final ObjArray requirementItemsArr = ObjArray.ofArrObj(true);

        private String requirementType;

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.enabled = API.readMemoryBoolean(address + 0x24);
            this.completed = API.readMemoryBoolean(address, 0x34);
            this.description = API.readMemoryString(address, 0x60);
            this.goalReached = API.readMemoryDouble(address + 0x78);
            this.goal = API.readMemoryDouble(address + 0x80);

            long definitionAddr = API.readMemoryPtr(address + 0x58);
            this.requirementType = API.readMemoryString(definitionAddr, 0x28);

            requirementItemsArr.update(API.readMemoryPtr(address + 0x48));
            requirementItemsArr.sync(requirementItems, Requirement::new);
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public double getProgress() {
            return goalReached;
        }

        @Override
        public double getGoal() {
            return goal;
        }

        @Override
        public boolean isCompleted() {
            return completed;
        }

        @Override
        public String getType() {
            return requirementType;
        }

        @Override
        public List<? extends QuestAPI.Requirement> getRequirements() {
            return requirementItems;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}