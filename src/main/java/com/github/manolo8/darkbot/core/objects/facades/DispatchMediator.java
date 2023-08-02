package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.API;
import eu.darkbot.api.managers.DispatchAPI;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

@Getter
public class DispatchMediator extends Updatable implements API.Singleton {
    @Getter(AccessLevel.NONE)
    private final ObjArray availableRetrieverArr = ObjArray.ofVector(true),
            inProgressRetrieverArr = ObjArray.ofVector(true);

    private int availableSlots, totalSlots;
    private final List<Retriever> availableRetrievers = new ArrayList<>();
    private final List<Retriever> inProgressRetrievers = new ArrayList<>();
    private final Retriever selectedRetriever = new Retriever();

    @Override
    public void update() {
        long dispatchRetrieverData = API.readMemoryPtr(address + 0x50);
        availableSlots = API.readMemoryInt(dispatchRetrieverData + 0x40);
        totalSlots = API.readMemoryInt(dispatchRetrieverData + 0x44);

        availableRetrieverArr.update(API.readMemoryPtr(dispatchRetrieverData + 0x58));
        availableRetrieverArr.sync(availableRetrievers, Retriever::new);

        inProgressRetrieverArr.update(API.readMemoryPtr(dispatchRetrieverData + 0x60));
        inProgressRetrieverArr.sync(inProgressRetrievers, Retriever::new);

        selectedRetriever.update(API.readMemoryPtr(dispatchRetrieverData + 0x68));
    }

    @Getter
    @ToString
    private static class Retriever extends Auto implements DispatchAPI.Retriever {
        private int id = -1, slotId = -1, tier = -1;
        private String iconId = "", type = "", name = "", descriptionId = "";
        private double duration = -1;
        private boolean isAvailable = false;

        @Getter(AccessLevel.NONE)
        private final ObjArray costListArr = ObjArray.ofVector(true);
        private final List<Cost> costList = new ArrayList<>();
        private final Cost instantCost = new Cost();

        @Override
        public void update() {
            if (address <= 0) return;

            this.isAvailable = API.readMemoryBoolean(address + 0x20);

            long dispatchModule = API.readMemoryPtr(address + 0x30);
            this.slotId = API.readMemoryInt(dispatchModule + 0x24); // 0 for available, 1 to 5 for in-progress
            this.duration = API.readMemoryDouble(dispatchModule + 0x28); // time left in seconds, or total time

            long retrieverDefinition = API.readMemoryPtr(address + 0x38);
            this.id = API.readMemoryInt(retrieverDefinition + 0x20); // 1 to 18
            this.tier = API.readMemoryInt(retrieverDefinition + 0x24); // 1, 2, 3, 4, 5 or 6
            this.iconId = API.readMemoryString(retrieverDefinition, 0x30); // dispatch_retriever_r01
            this.name = API.readMemoryString(retrieverDefinition, 0x38); // R-01
            this.type = API.readMemoryString(retrieverDefinition, 0x40); // resource
            this.descriptionId = API.readMemoryString(retrieverDefinition, 0x48); // dispatch_label_description_retriever_r01
            this.costListArr.update(API.readMemoryPtr(retrieverDefinition + 0x50));
            this.costListArr.sync(costList, Cost::new);

            this.instantCost.update(API.readMemoryPtr(retrieverDefinition + 0x58));
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
            this.amount = API.readMemoryInt(address + 0x20);
            this.lootId = API.readMemoryString(address, 0x28);
        }

    }

    public void overrideSelectedRetriever(DispatchAPI.Retriever retriever) {
        if (selectedRetriever == retriever) return;
        long value = retriever == null ? 0L : ((DispatchMediator.Retriever) retriever).address;
        Main.API.writeLong(Main.API.readMemoryPtr(address + 0x50) + 0x68, value);
    }
}
