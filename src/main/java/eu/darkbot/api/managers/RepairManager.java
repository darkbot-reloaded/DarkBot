package eu.darkbot.api.managers;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

public interface RepairManager {

    boolean isDestroyed();

    @Nullable
    String getLastDestroyerName();

    @Nullable
    LocalDateTime getLastDeathTime();
}
