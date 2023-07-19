package com.github.manolo8.darkbot.core.objects.facades;

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
        long dispatchRetrieverData = API.readMemoryPtr(address + 80);
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
    public static class Retriever extends Auto implements DispatchAPI.Retriever {
        private String id, type, name, descriptionId = "";
        private double duration = -1;
        private int slotId = -1;

        @Override
        public void update() {
            if (address <= 0) return;
            long dispatchModule = API.readMemoryPtr(address + 0x30);

            this.slotId = API.readMemoryInt(dispatchModule + 0x24); // 1
            this.name = API.readMemoryString(dispatchModule, 0x30); // dispatch_retriever_r01
            this.type = API.readMemoryString(dispatchModule, 0x38); // resource
            this.id = API.readMemoryString(dispatchModule, 0x48); // R-01
            this.descriptionId = API.readMemoryString(dispatchModule, 0x50); // dispatch_label_description_retriever_r01
            this.duration = API.readMemoryDouble(dispatchModule + 0x58); // time in seconds
        }

    }
}
