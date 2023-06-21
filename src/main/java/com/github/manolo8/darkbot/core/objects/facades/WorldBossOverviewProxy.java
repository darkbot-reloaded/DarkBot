package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.managers.WorldBossOverviewAPI;

import static com.github.manolo8.darkbot.Main.API;

public class WorldBossOverviewProxy extends Updatable implements WorldBossOverviewAPI {
    int attempts, currentTier;
    String bossName, bannerKey;

    @Override
    public void update() {
        if (address == 0) return;
        long data = API.readMemoryLong(address + 0x30) & ByteUtils.ATOM_MASK;

        long currentTierData = API.readMemoryLong(data + 0x50) & ByteUtils.ATOM_MASK;
        this.currentTier = API.readInt(currentTierData + 0x28);

        long attemptsData = API.readMemoryLong(data + 0x60) & ByteUtils.ATOM_MASK;
        this.attempts = API.readInt(attemptsData + 0x28);

        this.bossName = API.readString(data, 0x68);
        this.bannerKey = API.readString(data, 0x70);
    }

    @Override
    public int getAttempts() {
        return attempts;
    }

    @Override
    public int getCurrentTier() {
        return currentTier;
    }

    @Override
    public String getBossName() {
        return bossName;
    }

    @Override
    public String getBannerKey() {
        return bannerKey;
    }

    @Override
    public Status getStatus() {
        if (attempts > 0)
            return Status.AVAILABLE;
        else
            return Status.COMPLETED;

    }
}
