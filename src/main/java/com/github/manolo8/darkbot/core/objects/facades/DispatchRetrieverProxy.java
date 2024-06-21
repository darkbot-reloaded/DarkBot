package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import eu.darkbot.api.API;
import eu.darkbot.api.managers.DispatchAPI;
import lombok.Getter;
import lombok.ToString;

import static com.github.manolo8.darkbot.Main.API;

@Getter
public class DispatchRetrieverProxy extends Updatable implements API.Singleton {
    private int availableSlots, totalSlots;
    private final FlashList<Retriever> availableRetrievers = FlashList.ofVector(Retriever::new);
    private final FlashList<Retriever> inProgressRetrievers = FlashList.ofVector(Retriever::new);
    private final Retriever selectedRetriever = new Retriever();

    @Override
    public void update() {
        long dispatchRetrieverData = API.readAtom(address + 0x30);
        availableSlots = API.readInt(dispatchRetrieverData + 0x40);
        totalSlots = API.readInt(dispatchRetrieverData + 0x44);

        availableRetrievers.update(API.readAtom(dispatchRetrieverData + 0x58));
        inProgressRetrievers.update(API.readAtom(dispatchRetrieverData + 0x60));

        selectedRetriever.updateIfChanged(API.readAtom(dispatchRetrieverData + 0x68));
        selectedRetriever.update();
    }

    @Getter
    @ToString
    private static class Retriever extends Updatable implements DispatchAPI.Retriever {
        private int id = -1, slotId = -1, tier = -1;
        private String iconId = "", type = "", name = "", descriptionId = "";
        private double duration = -1;
        private boolean isAvailable = false;

        private final FlashList<Cost> costList =  FlashList.ofVector(Cost::new);
        private final Cost instantCost = new Cost();

        @Override
        public void update(long address) {
            super.update(address);

            long retrieverDefinition = readAtom(0x38);
            this.id = API.readInt(retrieverDefinition, 0x20); // 1 to 18
            this.tier = API.readInt(retrieverDefinition, 0x24); // 1, 2, 3, 4, 5 or 6
            this.iconId = API.readString(retrieverDefinition, 0x30); // dispatch_retriever_r01
            this.name = API.readString(retrieverDefinition, 0x38); // R-01
            this.type = API.readString(retrieverDefinition, 0x40); // resource
            this.descriptionId = API.readString(retrieverDefinition, 0x48); // dispatch_label_description_retriever_r01
            this.costList.update(API.readAtom(retrieverDefinition, 0x50));

            this.instantCost.update(API.readAtom(retrieverDefinition, 0x58));
        }

        @Override
        public void update() {
            if (address <= 0) return;

            this.isAvailable = API.readBoolean(address + 0x20);

            long dispatchModule = API.readAtom(address + 0x30);
            this.slotId = API.readInt(dispatchModule + 0x20); // 0 for available, 1 to 5 for in-progress
            this.duration = API.readDouble(dispatchModule + 0x28); // time left in seconds, or total time
        }

    }

    @Getter
    @ToString
    private static class Cost extends Auto implements DispatchAPI.Cost {
        private String lootId = "";
        private int amount = -1;

        @Override
        public void update() {
            if (address <= 0) return;
            this.amount = API.readInt(address + 0x20);
            this.lootId = API.readString(address, 0x28);
        }

    }

    public void overrideSelectedRetriever(DispatchAPI.Retriever retriever) {
        if (selectedRetriever == retriever) return;
        long value = retriever == null ? 0L : ((DispatchRetrieverProxy.Retriever) retriever).address;
        Main.API.writeLong(Main.API.readAtom(address + 0x30) + 0x68, value);
    }
}
