package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.facades.QuestProxy.Quest;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;

import eu.darkbot.api.managers.QuestAPI;
import eu.darkbot.api.managers.SeassonPassAPI;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

import static com.github.manolo8.darkbot.Main.API;

import java.util.List;

import org.jetbrains.annotations.Nullable;

public class SeassonPassMediator extends Updatable implements SeassonPassAPI {

    private final FlashList<SeassonPassQuest> dailyQuests = FlashList.ofVector(SeassonPassQuest.class);
    private final FlashList<SeassonPassQuest> weeklyQuests = FlashList.ofVector(SeassonPassQuest.class);
    private final FlashList<SeassonPassQuest> seassonQuests = FlashList.ofVector(SeassonPassQuest.class);
    private final FlashList<SeassonPassQuest> allQuests = FlashList.ofVector(SeassonPassQuest.class);

    @Getter
    private final CurrentLevelProgress currentLevelProgress = new CurrentLevelProgress();

    @Getter
    private final CurrentLevelProgress currentSeassonLevelInfo = new CurrentLevelProgress();

    @Getter
    private boolean seassonPassAvailable = false;

    private boolean dailyQuestUpdated = false;
    private boolean weeklyQuestUpdated = false;
    private boolean seassonQuestUpdated = false;
    private boolean allQuestsUpdated = false;

    private long questDataAddr = 0;

    @Override
    public void update() {
        if (address == 0) {
            return;
        }

        this.allQuestsUpdated = false;
        this.dailyQuestUpdated = false;
        this.weeklyQuestUpdated = false;
        this.seassonQuestUpdated = false;

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
        if (!dailyQuestUpdated && questDataAddr != 0) {
            dailyQuestUpdated = true;
            dailyQuests.update(API.readAtom(questDataAddr + 0x60));
        }

        return this.dailyQuests;
    }

    public @Nullable List<? extends SeassonPassQuest> getWeeklyQuests() {
        if (!weeklyQuestUpdated && questDataAddr != 0) {
            weeklyQuestUpdated = true;
            weeklyQuests.update(API.readAtom(questDataAddr + 0x68));
        }

        return this.weeklyQuests;
    }

    public @Nullable List<? extends SeassonPassQuest> getSeassonQuests() {
        if (!seassonQuestUpdated && questDataAddr != 0) {
            seassonQuestUpdated = true;
            seassonQuests.update(API.readAtom(questDataAddr + 0x70));
        }

        return this.seassonQuests;
    }

    public @Nullable List<? extends SeassonPassQuest> getAllQuests() {
        if (!allQuestsUpdated && questDataAddr != 0) {
            allQuestsUpdated = true;
            allQuests.update(API.readAtom(questDataAddr + 0x58));
        }

        return this.allQuests;
    }

    @Getter
    @ToString
    public static class CurrentLevelProgress extends Auto implements SeassonPassAPI.CurrentLevelProgress {
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
            return maxProgress == 0 ? 1d : (double) this.currentProgress / this.maxProgress;
        }
    }

    @Getter
    @ToString
    public static class SeassonPassQuest extends Updatable implements SeassonPassAPI.SeassonPassQuest {
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
            this.status = API.readInt(API.readAtom(address + 0x48) + 0x20);

            this.quest.update(API.readAtom(address, 0x40));
        }

        public @Nullable QuestAPI.Quest getQuest() {
            return this.quest.address == 0 ? null : quest;
        }
    }

}
