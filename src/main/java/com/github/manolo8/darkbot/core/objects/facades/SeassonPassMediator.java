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

    private final FlashList<SeassonPassQuest> allQuests = FlashList.ofVector(SeassonPassQuest.class);

    @Override
    public void update() {
        if (address == 0) {
            return;
        }

        long data = API.readMemoryPtr(address + 0x60);

        this.seassonPassAvailable = API.readBoolean(API.readMemoryPtr(data + 0x70) + 0x20);

        if (!seassonPassAvailable) {
            return;
        }

        this.currentLevelProgress.update(API.readMemoryPtr(data + 0x58));
        this.currentLevelInfo.update(API.readMemoryPtr(data + 0x68));

        long questDataAddr = API.readMemoryPtr(address + 0x78);
        allQuests.update(API.readMemoryPtr(questDataAddr + 0x58));
    }

    public List<? extends SeassonPassQuest> getSeassonQuests() {
        return this.allQuests;
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

            this.max = API.readMemoryInt(address + 0x24);
            this.current = API.readMemoryInt(address + 0x28);
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

        @Getter(AccessLevel.NONE)
        private final QuestProxy.Quest quest = new Quest();

        @Override
        public void update() {
            this.isGoldMission = readBoolean(20);
            this.goldLocked = readBoolean(24);
            this.oncePreMission = readBoolean(28);

            this.quest.update(API.readMemoryPtr(address, 0x40));
        }

        public @Nullable QuestAPI.Quest getQuest() {
            return quest.address == 0 ? null : quest;
        }
    }

}
