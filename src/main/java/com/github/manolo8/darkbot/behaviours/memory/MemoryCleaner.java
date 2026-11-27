package com.github.manolo8.darkbot.behaviours.memory;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.utils.AvmUtils;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.plugins.PluginIssue;
import eu.darkbot.api.extensions.Behavior;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.util.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Releases objects that the game client leaks, so its memory doesn't grow until a refresh.
 * <p>
 * Each leak is handled by one {@link LeakCleaner}, run at its own interval. This class only applies the setting,
 * resets the cleaners when the client reloads, and keeps a failing cleaner from affecting the others.
 * What was released is printed in one line when the bot stops or the cleaner is turned off.
 * Only client memory of objects that are already gone is touched, nothing is sent to the server.
 */
@Feature(name = "Memory cleaner",
        description = "Releases objects the game client leaks, so memory doesn't grow until a refresh")
public class MemoryCleaner implements Behavior {

    private final Main main;
    private final BotInstaller botInstaller;
    private final List<ScheduledCleaner> cleaners = new ArrayList<>();

    private boolean enabled, running;
    private long screenManager, core;

    public MemoryCleaner(Main main, BotInstaller botInstaller) {
        this.main = main;
        this.botInstaller = botInstaller;
        cleaners.add(new ScheduledCleaner(new ShipReferenceCleaner(main, botInstaller)));
        cleaners.add(new ScheduledCleaner(new TooltipCleaner(main.facadeManager.itemsControlMediator)));
    }

    @Override
    public void onTickBehavior() {
        tick();
    }

    /** Also called while the bot runs, during map changes and deaths: the bot's own state tells a stop apart. */
    @Override
    public void onStoppedBehavior() {
        tick();
    }

    private void tick() {
        boolean botRunning = main.isRunning();
        if (running && !botRunning) reportReleased();
        running = botRunning;

        boolean wanted = main.config.MISCELLANEOUS.MEMORY_CLEANER;
        if (wanted != enabled) {
            if (!wanted) reportReleased();
            enabled = wanted;
            resetCleaners();
        }
        if (!enabled || !isAnyCleanerDue() || !detectClient()) return;

        for (ScheduledCleaner cleaner : cleaners) cleaner.tick();
    }

    /** Whether a cleaner runs now. The client is only checked then, not on every bot tick. */
    private boolean isAnyCleanerDue() {
        for (ScheduledCleaner cleaner : cleaners) if (cleaner.isDue()) return true;
        return false;
    }

    /**
     * Follows the running client, recognized by its screen manager and VM core. When either changes, the client
     * reloaded and the objects the cleaners found are gone. False while there is no client.
     */
    private boolean detectClient() {
        Long address = botInstaller.screenManagerAddress.get();
        if (address == null || address == 0) return false;
        long currentCore = AvmUtils.vmCore(address);
        if (currentCore == 0) return false;
        if (address == screenManager && currentCore == core) return true;

        if (screenManager != 0) for (ScheduledCleaner cleaner : cleaners) cleaner.clientChanged();
        screenManager = address;
        core = currentCore;
        return true;
    }

    /** Prints what was released since the last report, if anything. */
    private void reportReleased() {
        StringJoiner sinceReport = new StringJoiner(" and "), total = new StringJoiner(" and ");
        for (ScheduledCleaner cleaner : cleaners) {
            long count = cleaner.releasedSinceReport();
            if (count == 0) continue;
            sinceReport.add(count + " " + cleaner.cleaner.getReleasedName());
            total.add(Long.toString(cleaner.cleaner.getReleased()));
        }
        if (sinceReport.length() > 0)
            System.out.println("Memory cleaner: released " + sinceReport + " (" + total + " in total)");
    }

    private void resetCleaners() {
        for (ScheduledCleaner cleaner : cleaners) cleaner.reset();
    }

    /**
     * Reports a failed run as a warning of this feature, the way the bot reports errors of other features: the
     * feature keeps running and an identical error is counted instead of printed again.
     */
    private void onFailure(RuntimeException e) {
        FeatureDefinition<MemoryCleaner> feature = main.featureRegistry.getFeatureDefinition(this);
        if (feature != null) feature.getIssues().handleTickFeatureException(PluginIssue.Level.WARNING, e);
        else e.printStackTrace();
    }

    /**
     * Runs one cleaner at its interval. A failed run may have left the cleaner's state half updated, so the cleaner
     * forgets it and starts over at its next interval, while the other cleaners keep running. Only the first failure
     * of a row is reported: errors whose message changes every run would otherwise each be a new issue.
     */
    private final class ScheduledCleaner {
        private final LeakCleaner cleaner;
        private final Timer interval;
        private boolean failing;
        private long reportedReleased;

        private ScheduledCleaner(LeakCleaner cleaner) {
            this.cleaner = cleaner;
            this.interval = Timer.get(cleaner.getIntervalMs());
        }

        private boolean isDue() {
            return interval.isInactive();
        }

        private long releasedSinceReport() {
            long released = cleaner.getReleased();
            long count = released - reportedReleased;
            reportedReleased = released;
            return count;
        }

        private void tick() {
            if (!interval.tryActivate()) return;
            try {
                cleaner.clean();
                failing = false;
            } catch (RuntimeException e) {
                cleaner.reset();
                if (!failing) onFailure(e);
                failing = true;
            }
        }

        private void reset() {
            cleaner.reset();
            interval.disarm();
            failing = false;
        }

        private void clientChanged() {
            cleaner.clientChanged();
            interval.disarm();
            failing = false;
        }
    }
}
