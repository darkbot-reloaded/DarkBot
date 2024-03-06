package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import eu.darkbot.api.API;
import lombok.Getter;

@Getter
public class DispatchGateProxy extends Updatable implements API.Singleton {

    @Override
    public void update() {
//        long dispatchGateData = API.readMemoryPtr(address + 0x30);
    }
}
