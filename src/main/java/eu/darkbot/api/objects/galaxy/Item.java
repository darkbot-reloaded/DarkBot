package eu.darkbot.api.objects.galaxy;

import java.time.Instant;

public interface Item {

    Instant getDate();

    String getState();
    String getType();

    int getGateId();
    int getDuplicate();
    int getPartId();
    int getItemId();
    int getAmount();
    int getCurrent();
    int getTotal();
    int getMultiplierUsed();
}
