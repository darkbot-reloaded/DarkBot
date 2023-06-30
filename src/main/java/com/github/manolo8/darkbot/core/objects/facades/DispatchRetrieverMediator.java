package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
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
        long dispatchRetrieverData = API.readMemoryLong(address + 80) & ByteUtils.ATOM_MASK;
        availableSlots = API.readMemoryInt(dispatchRetrieverData + 0x40);
        totalSlots = API.readMemoryInt(dispatchRetrieverData + 0x44);

        availableRetrieverArr.update(API.readMemoryLong(dispatchRetrieverData + 0x58) & ByteUtils.ATOM_MASK);
        availableRetrieverArr.sync(availableRetrievers, Retriever::new);
        inProgressRetrieverArr.update(API.readMemoryLong(dispatchRetrieverData + 0x60) & ByteUtils.ATOM_MASK);
        inProgressRetrieverArr.sync(inProgressRetrievers, Retriever::new);

        selectedRetriever.update(API.readMemoryLong(dispatchRetrieverData + 0x68) & ByteUtils.ATOM_MASK);
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
        public String id, name, description;
        public double duration;
        public int slotId;

        @Override
        public void update() {
            if (address <= 0) return;
            long dispatchModule = API.readMemoryLong(address + 0x30) & ByteUtils.ATOM_MASK;

            this.slotId = API.readMemoryInt(dispatchModule + 0x24);
            this.id = API.readMemoryString(dispatchModule, 0x28);
            this.name = API.readMemoryString(dispatchModule, 0x50);
            this.description = API.readMemoryString(dispatchModule, 0x48);
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
        public String getDescription() {
            return description;
        }

        @Override
        public double getDuration() {
            return duration;
        }

        @Override
        public int getSlotId() {
            return slotId;
        }
    }
}
