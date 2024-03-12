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
    private CurrentLevelProgress currentLevelInfo = new CurrentLevelProgress();

    @Getter
    private boolean seassonPassAvailable = false;

    private final FlashList<SeassonPassQuest> dailyQuests = FlashList.ofVector(SeassonPassQuest.class);
    private final FlashList<SeassonPassQuest> weeklyQuests = FlashList.ofVector(SeassonPassQuest.class);
    private final FlashList<SeassonPassQuest> seassonQuests = FlashList.ofVector(SeassonPassQuest.class);

    @Override
    public void update() {
        if (address == 0) {
            return;
        }

        long data = API.readAtom(address + 0x60);

        this.seassonPassAvailable = API.readBoolean(API.readAtom(data + 0x70) + 0x20);

        if (!seassonPassAvailable) {
            return;
        }

        this.currentLevelProgress.update(API.readAtom(data + 0x58));
        this.currentLevelInfo.update(API.readAtom(data + 0x68));

        long questDataAddr = API.readAtom(address + 0x78);

        // allQuests.update(API.readMemoryPtr(questDataAddr + 0x58));

        dailyQuests.update(API.readAtom(questDataAddr + 0x60));
        weeklyQuests.update(API.readAtom(questDataAddr + 0x68));
        seassonQuests.update(API.readAtom(questDataAddr + 0x70));
    }

    public List<? extends SeassonPassQuest> getDailyQuests() {
        return this.dailyQuests;
    }

    public List<? extends SeassonPassQuest> getWeeklyQuests() {
        return this.weeklyQuests;
    }

    public List<? extends SeassonPassQuest> getSeassonQuests() {
        return this.seassonQuests;
    }

    @Getter
    @ToString
    public static class CurrentLevelProgress extends Auto {
        private int max;
        private int current;

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.max = API.readInt(address + 0x24);
            this.current = API.readInt(address + 0x28);
        }

        public double getProgressPercentage() {
            return this.current / this.max;
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
