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
    private Quest currentQuest = new Quest();

    private final List<Quest> questItems = new ArrayList<>();

    private final ObjArray questItemsArr = ObjArray.ofVector(true);

    private @Nullable QuestListItem questGiverSelected = new QuestListItem();

    @Override
    public void update() {
        if (address == 0) {
            return;
        }

        long questClass = API.readMemoryLong(address + 0x98) & ByteUtils.ATOM_MASK;

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
        questItemsArr.sync(questItems, Quest::new);

        long questGiverSelectedAddr = API.readMemoryLong(questClass + 0x0A8) & ByteUtils.ATOM_MASK;
        if (questGiverSelectedAddr == 0) {
            this.questGiverSelected = null;
        } else {
            if (this.questGiverSelected == null) {
                this.questGiverSelected = new QuestListItem();
            }

            this.questGiverSelected.update(questGiverSelectedAddr);
        }
    }

    public @Nullable Quest getCurrentQuest() {
        return currentQuest;
    }

    public static class QuestListItem extends Auto {
        private boolean selected;
        private boolean completed;
        private String title;
        private String type;

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.selected = API.readBoolean(address + 0x34);
            this.completed = API.readBoolean(address + 0x38);
            this.title = API.readMemoryString(address, 0x58);
            this.type = API.readMemoryString(address, 0x70);
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
    }

    public static class Quest extends Auto {
        private boolean active;
        private String category;
        private String description;
        private String title;
        private int conditionsCount;
        private int id;

        private final List<Contition> conditionItems = new ArrayList<>();
        private final ObjArray conditionItemsArr = ObjArray.ofVector(true);

        private final List<Reward> rewardItems = new ArrayList<>();
        private final ObjArray rewardItemsArr = ObjArray.ofVector(true);

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

            long conditionsAddr = API.readMemoryLong(address + 0x38) & ByteUtils.ATOM_MASK;
            this.conditionsCount = API.readMemoryInt(conditionsAddr + 0x30);

            conditionItemsArr.update(API.readMemoryPtr(conditionsAddr + 0x40));
            conditionItemsArr.sync(conditionItems, Contition::new);

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

        public int getConditionsCount() {
            return conditionsCount;
        }
    }

    public static class Reward extends Auto {
        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            System.out.println(address);
        }
    }

    public static class Contition extends Auto {
        private String description;
        private double goalReached;
        private double goal;

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.description = API.readMemoryString(address, 0x5F);
            this.goalReached = API.readMemoryDouble(address + 0x77);
            this.goal = API.readMemoryDouble(address + 0x7F);
        }

        public String getDescription() {
            return description;
        }

        public double getGoalReached() {
            return goalReached;
        }

        public double getGoal() {
            return goal;
        }
    }
}