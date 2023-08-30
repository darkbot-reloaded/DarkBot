package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.itf.NativeUpdatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.core.utils.TimeSeriesImpl;
import com.github.manolo8.darkbot.modules.DisconnectModule;
import com.github.manolo8.darkbot.utils.I18n;
import eu.darkbot.api.game.stats.Stats;
import eu.darkbot.api.managers.EventBrokerAPI;
import eu.darkbot.api.managers.StatsAPI;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.manolo8.darkbot.Main.API;

public class StatsManager implements Manager, StatsAPI, NativeUpdatable {

    private final Main main;
    private final EventBrokerAPI eventBroker;

    private long address;

    public int userId;
    public volatile String sid;
    public volatile String instance;
  
  
    private int teleportBonusAmount;
    private boolean premium;

    private long address;
    private long started = System.currentTimeMillis();
    private long runningTime = Time.SECOND; // Assume running for 1 second by default
    private boolean lastStatus;
    private final Map<StatKey, StatImpl> statistics = new HashMap<>();

    private final StatImpl runtime;
    private final StatImpl credits, uridium, experience, honor, cargo, maxCargo, novaEnergy;
    private final AverageStats cpuStat, pingStat, tickStat, memoryStat;

    private HashMap<BootyKeyType, Integer> bootyKeyValues = new HashMap<BootyKeyType, Integer>();

    public StatsManager(Main main, EventBrokerAPI eventBroker) {
        this.main = main;
        this.eventBroker = eventBroker;

        register(Stats.Bot.RUNTIME, runtime = createStat());

        register(Stats.General.CREDITS, credits = createStat());
        register(Stats.General.URIDIUM, uridium = createStat());
        register(Stats.General.EXPERIENCE, experience = createStat());
        register(Stats.General.HONOR, honor = createStat());
        register(Stats.General.CARGO, cargo = createStat());
        register(Stats.General.MAX_CARGO, maxCargo = createStat());
        register(Stats.General.NOVA_ENERGY, novaEnergy = createStat());

        register(Stats.Bot.PING, pingStat = new AverageStats(false));
        register(Stats.Bot.TICK_TIME, tickStat = new AverageStats(true));
        register(Stats.Bot.MEMORY, memoryStat = new AverageStats(false));
        register(Stats.Bot.CPU, cpuStat = new AverageStats(true));
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.invalid.add(value -> {
            address = 0;
            userId = 0;
        });
        botInstaller.heroInfoAddress.add(value -> address = value);
    }

    public void tick() {
        updateNonZero(runtime, System.currentTimeMillis());

        if (address == 0) return;

        updateNonZero(credits, readDouble(352));
        updateNonZero(uridium, readDouble(360));
        updateNonZero(experience, readDouble(376));
        checkHonor(updateNonZero(honor, readDouble(384)));

        cargo.track(readIntHolder(304));
        maxCargo.track(readIntHolder(312));

        sid = readString(200);
        userId = readInt(48);
        if (main.settingsManager.getAddress() != 0) {
            instance = main.settingsManager.readString(664);
        }

        teleportBonusAmount = API.readMemoryInt(address + 0x50);

        updateBootyKeys();

        premium = API.readMemoryBoolean((API.readMemoryLong(address + 0xF0) & ByteUtils.ATOM_MASK) + 0x20);
        novaEnergy.track(readInt(0x100, 0x28));
    }

    @Override
    public Stat getStat(Key key) {
        return statistics.get(StatKey.of(key));
    }

    @Override
    public Stat registerStat(Key key) {
        if (key.namespace() == null) throw new UnsupportedOperationException();
        StatImpl stat = createStat();
        register(key, stat);
        return stat;
    }

    private void updateBootyKeys() {
        for (BootyKeyType type : BootyKeyType.values()) {
            bootyKeyValues.put(type, API.readMemoryInt(address + type.getOffset()));
        }
    }

    private void register(Key key, StatImpl stat) {
        statistics.put(StatKey.of(key), stat);
    }

    @Override
    public void setStatValue(Key key, double v) {
        if (key.namespace() == null) throw new UnsupportedOperationException();
        StatImpl stat = statistics.get(StatKey.of(key));
        if (stat != null) stat.track(v);
    }

    private double updateNonZero(StatImpl stat, double value) {
        if (value == 0) return 0;
        return stat.track(value);
    }

    public int getLevel() {
        return Math.max(1, (int) (Math.log(getTotalExperience() / 10_000) / Math.log(2)) + 2);
    }

    public void tickAverageStats(long tickTime) {
        int p = getPing();
        if (p > 0) pingStat.track(p);

        cpuStat.track(API.getCpuUsage());
        tickStat.track(tickTime);
        memoryStat.track(API.getMemoryUsage());
    }

    private void checkHonor(double honorDiff) {
        if (honorDiff > -10_000) return;

        System.out.println("Paused bot, lost " + honorDiff + " honor.");
        double friendlies = Math.log(Math.abs(honorDiff) / 100) / Math.log(2);
        boolean isExact = Math.abs(friendlies - Math.round(friendlies)) < 0.01;
        System.out.println("Look like " + friendlies + " friendly kills, credible & pausing: " + isExact);

        if (!main.config.MISCELLANEOUS.HONOR_LOST_EXACT || isExact)
            main.setModule(new DisconnectModule(null, I18n.get("module.disconnect.reason.honor")));
    }

    private boolean updateStats() {
        return main.isRunning() || main.config.MISCELLANEOUS.UPDATE_STATS_WHILE_PAUSED;
    }

    public void resetValues() {
        statistics.values().forEach(StatImpl::reset);
        eventBroker.sendEvent(new StatsResetEvent());
    }

    @Override
    public int getPing() {
        return main.pingManager.ping;
    }

    @Override
    public Duration getRunningTime() {
        return Duration.ofMillis((long) runtime.getEarned());
    }

    @Override
    public void resetStats() {
        resetValues();
    }


    public AverageStats getCpuStats() {
        return cpuStat;
    }

    public AverageStats getPingStats() {
        return pingStat;
    }

    public AverageStats getTickStats() {
        return tickStat;
    }

    public AverageStats getMemoryStats() {
        return memoryStat;
    }

    @Override
    public long getAddress() {
        return address;
    }


    @Value
    @Accessors(fluent = true)
    private static class StatKey implements StatsAPI.Key {
        String namespace;
        String category;
        String name;

        public static StatKey of(StatsAPI.Key key) {
            return new StatKey(key.namespace(), key.category(), key.name());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StatsAPI.Key)) return false;
            StatsAPI.Key other = (StatsAPI.Key) o;
            return Objects.equals(namespace, other.namespace())
                    && Objects.equals(category, other.category())
                    && Objects.equals(name, other.name());
        }

        @Override
        public int hashCode() {
            return Objects.hash(namespace, category, name);
        }
    }

    private StatImpl createStat() {
        return new StatImpl(this::updateStats);
    }

    @Getter
    @RequiredArgsConstructor
    private static class StatImpl implements Stat {
        private final Supplier<Boolean> trackDiff;
        protected double initial = Double.NaN;
        protected double earned, spent;
        protected double current;
        protected TimeSeriesImpl timeSeries = new TimeSeriesImpl();

    public int getTeleportBonusAmount() {
        return teleportBonusAmount;
    }

    public int getKeyAmountByType(BootyKeyType type) {
        if (bootyKeyValues.containsKey(type)) {
            return bootyKeyValues.get(type);
        }
        return 0;
    }

    public boolean isPremium() {
        return premium;
    }

    protected double track(double value) {
        double diff = value - this.current;

        if (Double.isNaN(initial)) {
            initial = value;
        } else if (trackDiff.get()) {
            if (diff > 0) earned += diff;
            else spent -= diff;
        }
        current = value;
        timeSeries.track(earned - spent);

        return diff;
    }

    private void reset() {
        earned = spent = 0;
    }

    @Override
    public @Nullable TimeSeries getTimeSeries() {
        return timeSeries;
    }

    public static class AverageStats extends StatImpl {
        private static final DecimalFormat ONE_PLACE_FORMAT = new DecimalFormat("0.0");

        @Getter
        private double average, max = Double.MIN_VALUE;
        private final boolean showDecimal;

        private long lastTime = System.currentTimeMillis();

        private Consumer<String> onChange;

        public AverageStats(boolean showDecimal) {
            super(() -> true);
            this.showDecimal = showDecimal;
        }

        protected double track(double value) {
            long now = System.currentTimeMillis();
            double diff = super.track(value);

            double adjustFactor = (now - lastTime) / 10_000d;
            average += adjustFactor * (value - average); // 1s of data would be 1/10th of the avg
            max += adjustFactor * 0.2 * (average - max); // 1s of data would be 1/50th of adjustment towards avg
            max = Math.max(max, value); // If this IS a max, keep it

            if (diff != 0 && onChange != null) {
                String s = showDecimal ? ONE_PLACE_FORMAT.format(value) : String.valueOf((int) value);
                onChange.accept(s);
            }
            lastTime = now;
            return diff;
        }

        public void setListener(Consumer<String> onChange) {
            this.onChange = onChange;
        }

        @Override
        public String toString() {
            return "Max=" + ONE_PLACE_FORMAT.format(getMax()) +
                    "\nAverage=" + ONE_PLACE_FORMAT.format(getAverage());
        }
    }

    public enum BootyKeyType {
        GREEN(0x54),
        BLUE(0x58),
        RED(0x5c),
        SILVER(0x60),
        APOCALYPSE(0x64),
        PROMETHEUS(0x68),
        OBSIDIAN_MICROCHIP(0x6c),
        BLACK_LIGHT_CODE(0x70),
        BLACK_LIGHT_DECODER(0x74),
        PROSPEROUS_FRAGMENT(0x78),
        ASTRAL(0x7c),
        ASTRAL_SUPREME(0x80),
        EMPYRIAN(0x84),
        LUCENT(0x88);

        private long offset;

        private BootyKeyType(long offset) {
            this.offset = offset;
        }

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT).replace("_", "-");
        }

        public long getOffset() {
            return offset;
        }
    }
}
