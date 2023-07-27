package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import eu.darkbot.api.API;

import static com.github.manolo8.darkbot.Main.API;

import org.jetbrains.annotations.Nullable;

public class QuestProxy extends Updatable implements API.Singleton {
    private Quest currentQuest = new Quest();

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
    }

    public @Nullable Quest getCurrenQuest() {
        return currentQuest;
    }

    public static class Quest extends Auto {
        private boolean active;
        private String category;
        private String description;
        private String title;
        private int conditionsCount;
        private int id;

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
        }

        public int getCurrentQuestId() {
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
}