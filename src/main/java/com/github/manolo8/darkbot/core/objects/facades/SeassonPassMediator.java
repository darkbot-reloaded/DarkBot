package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.facades.QuestProxy.Quest;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import eu.darkbot.api.managers.QuestAPI;
import eu.darkbot.api.managers.SeassonPassAPI;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

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

    private boolean allQuestsUpdated = false;

    private long questDataAddr = 0;

    @Override
    public void update() {
        if (address == 0) {
            return;
        }

        this.allQuestsUpdated = false;

        long data = API.readAtom(address + 0x60);
        this.seassonPassAvailable = API.readBoolean(API.readAtom(data + 0x70) + 0x20);

        if (!seassonPassAvailable) {
            return;
        }

        this.currentLevelProgress.update(API.readAtom(data + 0x58));
        this.currentSeassonLevelInfo.update(API.readAtom(data + 0x68));
        this.questDataAddr = API.readAtom(address + 0x78);

        if (questDataAddr == 0) {
            return;
        }
        dailyQuests.update(API.readAtom(questDataAddr + 0x60));
        weeklyQuests.update(API.readAtom(questDataAddr + 0x68));
        seassonQuests.update(API.readAtom(questDataAddr + 0x70));
    }

    public @Nullable List<? extends SeassonPassQuest> getDailyQuests() {
        return this.dailyQuests;
    }

    public @Nullable List<? extends SeassonPassQuest> getWeeklyQuests() {
        return this.weeklyQuests;
    }

    public @Nullable List<? extends SeassonPassQuest> getSeassonQuests() {
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
        public void update(long address) {
            super.update(address);
            this.quest.update(readAtom(0x40));
        }

        @Override
        public void update() {
            this.isGoldMission = readBoolean(20);
            this.goldLocked = readBoolean(24);
            this.oncePreMission = readBoolean(28);
            this.status = readInt(0x48, 0x20);

            this.quest.update();
        }

        public @Nullable QuestAPI.Quest getQuest() {
            return this.quest.address == 0 ? null : quest;
        }
    }

}
