package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import eu.darkbot.api.API;

import static com.github.manolo8.darkbot.Main.API;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public class QuestProxy extends Updatable implements API.Singleton {
    private @Nullable Quest currentQuest;

    private final List<QuestListItem> questItems = new ArrayList<>();

    private final ObjArray questItemsArr = ObjArray.ofArrObj(true);

    private @Nullable Quest questGiverSelected;
    private @Nullable QuestListItem questInfoGiverSelected;

    private boolean visibleQuestGiver;
    private int tabSelected;

    @Override
    public void update() {
        if (address == 0) {
            return;
        }

        long questClass = API.readMemoryLong(address + 0x98) & ByteUtils.ATOM_MASK;

        this.visibleQuestGiver = API.readMemoryBoolean(address, 0x40);
        this.tabSelected = API.readMemoryInt(address, 0X50);

        long currentQuestAddr = API.readMemoryLong(questClass + 0x28) & ByteUtils.ATOM_MASK;

        if (currentQuestAddr == 0) {
            this.currentQuest = null;
        } else {
            if (this.currentQuest == null) {
                this.currentQuest = new Quest();
            }

            this.currentQuest.update(currentQuestAddr);
        }

        questItemsArr.update(API.readMemoryPtr(0x58));
        questItemsArr.sync(questItems, QuestListItem::new);

        long questInfoGiverSelectedAddr = API.readMemoryLong(address + 0xA8) & ByteUtils.ATOM_MASK;
        if (questInfoGiverSelectedAddr == 0) {
            this.questInfoGiverSelected = null;
        } else {
            if (this.questInfoGiverSelected == null) {
                this.questInfoGiverSelected = new QuestListItem();
            }

            this.questInfoGiverSelected.update(questInfoGiverSelectedAddr);
        }

        long questGiverSelectedAddr = API.readMemoryLong(address + 0xB0) & ByteUtils.ATOM_MASK;
        if (questGiverSelectedAddr == 0) {
            this.questGiverSelected = null;
        } else {
            if (this.questGiverSelected == null) {
                this.questGiverSelected = new Quest();
            }

            this.questGiverSelected.update(questGiverSelectedAddr);
        }
    }

    public @Nullable Quest getDisplayedQuest() {
        return currentQuest;
    }

    public @Nullable Quest getSelectedQuest() {
        return questGiverSelected;
    }

    public @Nullable QuestListItem getSelectedQuestInfo() {
        return questInfoGiverSelected;
    }

    public @Nullable List<QuestListItem> getCurrestQuests() {
        return questItems;
    }

    public boolean isQuestGiverOpen() {
        return visibleQuestGiver;
    }

    public int getSelectedTab() {
        return tabSelected;
    }

    public static class QuestListItem extends Auto {
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

        public int getId() {
            return id;
        }

        public int getLevelRequired() {
            return levelRequired;
        }

        public boolean isSelected() {
            return selected;
        }

        public boolean isCompleted() {
            return completed;
        }

        public String getTitle() {
            return title;
        }

        public String getType() {
            return type;
        }

        public boolean isActivable() {
            return activable;
        }
    }

    public static class Quest extends Auto {
        private boolean active;
        private String category;
        private String description;
        private String title;
        private int requirementCount;
        private int id;
        private boolean completed;

        private final List<Requirement> requirementItems = new ArrayList<>();
        private final ObjArray requirementItemsArr = ObjArray.ofArrObj(true);

        private final List<Reward> rewardItems = new ArrayList<>();
        private final ObjArray rewardItemsArr = ObjArray.ofArrObj(true);

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.id = API.readMemoryInt(address + 0x20);
            this.active = API.readMemoryBoolean(address + 0x24);
            this.category = API.readMemoryString(address, 0x48);
            this.title = API.readMemoryString(address, 0x68);
            this.description = API.readMemoryString(address, 0x70);

            long requirementAddr = API.readMemoryLong(address + 0x38) & ByteUtils.ATOM_MASK;
            this.requirementCount = API.readMemoryInt(requirementAddr + 0x30);
            this.completed = API.readMemoryBoolean(requirementAddr, 0x38);

            requirementItemsArr.update(API.readMemoryPtr(requirementAddr + 0x40));
            requirementItemsArr.sync(requirementItems, Requirement::new);

            rewardItemsArr.update(API.readMemoryPtr(address + 0x50));
            rewardItemsArr.sync(rewardItems, Reward::new);
        }

        public int getId() {
            return id;
        }

        public boolean isActive() {
            return active;
        }

        public String getCategory() {
            return category;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public int getRequirementsCount() {
            return requirementCount;
        }

        public boolean isCompleted() {
            return completed;
        }

        public List<Requirement> getRequirements() {
            return requirementItems;
        }

        public List<Reward> getRewards() {
            return rewardItems;
        }
    }

    public static class Reward extends Auto {
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

        public int getAmount() {
            return amount;
        }

        public String getType() {
            return type;
        }
    }

    public static class Requirement extends Auto {
        private String description;
        private double goalReached;
        private double goal;
        private boolean completed;
        private boolean enabled;

        private final List<Requirement> requirementItems = new ArrayList<>();
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

            long definitionAddr = API.readMemoryLong(address + 0x58) & ByteUtils.ATOM_MASK;
            this.requirementType = API.readMemoryString(definitionAddr, 0x28);

            requirementItemsArr.update(API.readMemoryPtr(address + 0x48));
            requirementItemsArr.sync(requirementItems, Requirement::new);
        }

        public String getDescription() {
            return description;
        }

        public double getProgress() {
            return goalReached;
        }

        public double getGoal() {
            return goal;
        }

        public boolean isCompleted() {
            return completed;
        }

        public String getType() {
            return requirementType;
        }

        public List<Requirement> getRequirements() {
            return requirementItems;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }
}