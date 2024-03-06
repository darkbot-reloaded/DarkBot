package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import eu.darkbot.api.API;
import eu.darkbot.api.managers.DispatchAPI;
import lombok.Getter;
import lombok.ToString;

import static com.github.manolo8.darkbot.Main.API;

@Getter
public class DispatchGateProxy extends Updatable implements API.Singleton {
    private final FlashList<DispatchGate> availableGates = FlashList.ofVector(DispatchGate::new);

    @Override
    public void update() {
        long dispatchGateData = API.readAtom(address + 0x30);
        availableGates.update(API.readAtom(dispatchGateData + 0x48));
    }

    @Getter
    @ToString
    private static class DispatchGate extends Updatable implements DispatchAPI.Gate {
        private int status = -1, gateId = -1, duration = -1;
        private double timeLeft = -1;
        private String dispatchId = "", iconId = "", name = "";

        private final Cost cost = new Cost();

        @Override
        public void update() {
            long gateData = API.readAtom(address + 0x28);
            this.status = API.readInt(gateData + 0x20);
            this.timeLeft = API.readDouble(gateData + 0x28);
            long gateDefinition = API.readAtom(address + 0x30);

            this.gateId = API.readInt(gateDefinition + 0x24);
            this.duration = API.readInt(gateDefinition + 0x28);
            this.dispatchId = API.readString(gateDefinition, 0x30);
            this.iconId = API.readString(gateDefinition, 0x38);
            this.name = API.readString(gateDefinition, 0x40);
            this.cost.update(API.readAtom(gateDefinition + 0x48));
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
}
