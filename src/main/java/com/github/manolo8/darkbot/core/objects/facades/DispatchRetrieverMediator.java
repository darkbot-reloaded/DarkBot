package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.managers.DispatchAPI;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class DispatchRetrieverMediator extends Updatable {
    public int availableSlots, totalSlots;
    public List<Retriever> availableRetrievers = new ArrayList<>();
    private final ObjArray availableRetrieverArr = ObjArray.ofVector(true);
    public List<Retriever> inProgressRetrievers = new ArrayList<>();
    private final ObjArray inProgressRetrieverArr = ObjArray.ofVector(true);
    public Retriever selectedRetriever = new Retriever();

    @Override
    public void update() {
        long dispatchRetrieverData = API.readMemoryPtr(address + 80);
        availableSlots = API.readMemoryInt(dispatchRetrieverData + 0x40);
        totalSlots = API.readMemoryInt(dispatchRetrieverData + 0x44);

        availableRetrieverArr.update(API.readMemoryPtr(dispatchRetrieverData + 0x58));
        availableRetrieverArr.sync(availableRetrievers, Retriever::new);
        inProgressRetrieverArr.update(API.readMemoryPtr(dispatchRetrieverData + 0x60));
        inProgressRetrieverArr.sync(inProgressRetrievers, Retriever::new);

        selectedRetriever.update(API.readMemoryPtr(dispatchRetrieverData + 0x68));
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public List<? extends Retriever> getAvailableRetrievers() {
        return availableRetrievers;
    }

    public List<? extends Retriever> getInProgressRetrievers() {
        return inProgressRetrievers;
    }

    public Retriever getSelectedRetriever() {
        return selectedRetriever;
    }

    public static class Retriever extends Auto implements DispatchAPI.Retriever {
        public String id, name, descriptionId = "";
        public double duration = -1;
        public int slotId = -1;

        @Override
        public void update() {
            if (address <= 0) return;
            long dispatchModule = API.readMemoryPtr(address + 0x30);

            this.slotId = API.readMemoryInt(dispatchModule + 0x24);
            this.id = API.readMemoryString(dispatchModule, 0x28);
            this.name = API.readMemoryString(dispatchModule, 0x50);
            this.descriptionId = API.readMemoryString(dispatchModule, 0x48);
            this.duration = API.readMemoryDouble(dispatchModule + 0x58);
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescriptionId() {
            return descriptionId;
        }

        @Override
        public double getDuration() {
            return duration;
        }

        @Override
        public int getSlotId() {
            return slotId;
        }

        @Override
        public String toString(){
            return name + " - " + duration + " - " + slotId;
        }
    }
}
