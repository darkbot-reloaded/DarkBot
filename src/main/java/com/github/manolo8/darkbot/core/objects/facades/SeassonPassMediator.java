package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.facades.QuestProxy.Quest;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;

import eu.darkbot.api.managers.QuestAPI;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

import static com.github.manolo8.darkbot.Main.API;

import java.util.List;

import org.jetbrains.annotations.Nullable;

public class SeassonPassMediator extends Updatable {

    @Getter
    private CurrentLevelProgress currentLevelProgress = new CurrentLevelProgress();

    @Getter
    private CurrentLevelProgress currentSeassonLevelInfo = new CurrentLevelProgress();

    @Getter
    private boolean seassonPassAvailable = false;

    private boolean dailyQuestsUpdated = false;
    private final FlashList<SeassonPassQuest> dailyQuests = FlashList.ofVector(SeassonPassQuest.class);
    private boolean weeklyQuestsUpdated = false;
    private final FlashList<SeassonPassQuest> weeklyQuests = FlashList.ofVector(SeassonPassQuest.class);
    private boolean seassonQuestsUpdated = false;
    private final FlashList<SeassonPassQuest> seassonQuests = FlashList.ofVector(SeassonPassQuest.class);

    private long questDataAddr = 0;

    @Override
    public void update() {
        if (address == 0) {
            return;
        }

        this.dailyQuestsUpdated = false;
        this.weeklyQuestsUpdated = false;
        this.seassonQuestsUpdated = false;

        long data = API.readAtom(address + 0x60);
        this.seassonPassAvailable = API.readBoolean(API.readAtom(data + 0x70) + 0x20);

        if (!seassonPassAvailable) {
            return;
        }

        this.currentLevelProgress.update(API.readAtom(data + 0x58));
        this.currentSeassonLevelInfo.update(API.readAtom(data + 0x68));
        this.questDataAddr = API.readAtom(address + 0x78);
    }

    public @Nullable List<? extends SeassonPassQuest> getDailyQuests() {
        if (!dailyQuestsUpdated && questDataAddr != 0) {
            dailyQuestsUpdated = true;
            dailyQuests.update(API.readAtom(questDataAddr + 0x60));
        }

        return this.dailyQuests;
    }

    public @Nullable List<? extends SeassonPassQuest> getWeeklyQuests() {
        if (!weeklyQuestsUpdated && questDataAddr != 0) {
            weeklyQuestsUpdated = true;
            weeklyQuests.update(API.readAtom(questDataAddr + 0x68));
        }

        return this.weeklyQuests;
    }

    public @Nullable List<? extends SeassonPassQuest> getSeassonQuests() {
        if (!seassonQuestsUpdated && questDataAddr != 0) {
            seassonQuestsUpdated = true;
            seassonQuests.update(API.readAtom(questDataAddr + 0x70));
        }

        return this.seassonQuests;
    }

    @Getter
    @ToString
    public static class CurrentLevelProgress extends Auto {
        private int maxProgress;
        private int currentProgress;

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.maxProgress = API.readInt(address + 0x24);
            this.currentProgress = API.readInt(address + 0x28);
        }

        public double getProgressPercentage() {
            return this.currentProgress / this.maxProgress;
        }
    }

    @Getter
    @ToString
    public class SeassonPassQuest extends Updatable {
        private boolean isGoldMission;
        private boolean goldLocked;
        private boolean oncePreMission;

        private int status;

        @Getter(AccessLevel.NONE)
        private final QuestProxy.Quest quest = new Quest();

        @Override
        public void update() {
            this.isGoldMission = readBoolean(20);
            this.goldLocked = readBoolean(24);
            this.oncePreMission = readBoolean(28);

            /**
             * 0 = Not completed
             * 2 = Gold locked
             * 3 = Completed
             */
            this.status = API.readInt(API.readAtom(address + 0x48) + 0x20);

            this.quest.update(API.readAtom(address, 0x40));
        }

        public @Nullable QuestAPI.Quest getQuest() {
            return quest.address == 0 ? null : quest;
        }
    }

}
