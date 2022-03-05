package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.other.Health;
import eu.darkbot.api.game.other.Lockable;
import eu.darkbot.api.game.other.Point;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.I18nAPI;
import eu.darkbot.api.managers.PetAPI;
import eu.darkbot.api.managers.StatsAPI;
import eu.darkbot.api.utils.Inject;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Set;

@Feature(name = "Infos Drawer", description = "Draws infos about map, hero, health etc.")
public class InfosDrawer implements Drawable {

    private static final NumberFormat HEALTH_FORMAT;

    static {
        DecimalFormatSymbols sym = new DecimalFormatSymbols();
        sym.setGroupingSeparator(' ');
        HEALTH_FORMAT = new DecimalFormat("###,###,###", sym);
    }

    private final Main main;
    private final HeroAPI hero;
    private final PetAPI pet;
    private final I18nAPI i18n;
    private final StatsAPI stats;

    private final ConfigSetting<Set<DisplayFlag>> displayFlags;
    private final ConfigSetting<Boolean> resetRefresh;
    private final ConfigSetting<Integer> refreshTime;

    public InfosDrawer(PluginAPI api) {
        this(api.requireInstance(Main.class), api.requireAPI(HeroAPI.class),
                api.requireAPI(PetAPI.class), api.requireAPI(I18nAPI.class),
                api.requireAPI(StatsAPI.class), api.requireAPI(ConfigAPI.class));
    }

    @Inject
    public InfosDrawer(Main main, HeroAPI hero, PetAPI pet, I18nAPI i18n, StatsAPI stats, ConfigAPI config) {
        this.main = main;
        this.hero = hero;
        this.pet = pet;
        this.i18n = i18n;
        this.stats = stats;

        this.displayFlags = config.requireConfig("bot_settings.map_display.toggle");

        this.resetRefresh = config.requireConfig("miscellaneous.reset_refresh");
        this.refreshTime = config.requireConfig("miscellaneous.refresh_time");
    }

    @Override
    public void onDraw(MapGraphics mg) {
        drawInfos(mg);
        drawHealth(mg);
    }

    private void drawInfos(MapGraphics mg) {
        mg.setColor("text_dark");

        String status = i18n.get((main.isRunning() ? "gui.map.running" : "gui.map.waiting"), Time.toString(stats.getRunningTime().toMillis()));

        mg.drawString(status, mg.getMiddle(), mg.getHeight() / 2 + 35, MapGraphics.Align.MID);

        mg.setFont("small");
        String info = i18n.get("gui.map.info", main.getVersion().toString(), (main.isRunning() || !resetRefresh.getValue() ? Time.toString(System.currentTimeMillis() - main.lastRefresh) : "00"), Time.toString(refreshTime.getValue() * 60 * 1000L));

        mg.drawString(info, 5, 12, MapGraphics.Align.LEFT);
        if (main.getModule() != null) {
            mg.drawString(main.tickingModule ? main.getModule().getStatus() : main.getModule().getStoppedStatus(), 5, 26, MapGraphics.Align.LEFT);
        }

        mg.drawString(String.format("%.1ftick %dms ping", main.getTickTime(), stats.getPing()), mg.getWidth() - 5, 12, MapGraphics.Align.RIGHT);
        mg.drawString("SID: " + main.backpage.sidStatus(), mg.getWidth() - 5, 26, MapGraphics.Align.RIGHT);

        drawMap(mg);
    }

    protected void drawMap(MapGraphics mg) {
        mg.setColor("text_dark");
        mg.setFont("big");

        String name = hero.getMap().getId() == -1 ? I18n.get("gui.map.loading") : hero.getMap().getName();
        mg.drawString(name, mg.getMiddle(), mg.getHeight() / 2 - 5, MapGraphics.Align.MID);
    }

    private void drawHealth(MapGraphics mg) {
        mg.setColor("text");
        mg.setFont("mid");

        if (hasDisplayFlag(DisplayFlag.HERO_NAME))
            mg.drawString(hero.getEntityInfo().getUsername(), 10 + (mg.getMiddle() - 20) / 2, mg.getHeight() - 40, MapGraphics.Align.MID);

        Point pos = Point.of(10, mg.getHeight() - 34);
        drawHealth(mg, hero.getHealth(), pos, mg.getMiddle() - 20, 12, 0);

        if (pet.isValid() && hasDisplayFlag(DisplayFlag.SHOW_PET)) {
            pos = Point.of(10, mg.getHeight() - 52);
            drawHealth(mg, pet.getHealth(), pos, (int) ((mg.getMiddle() - 20) * 0.25), 6, 0);

            PetAPI.PetStat fuel = pet.getStat(PetAPI.Stat.FUEL);
            if (fuel != null) {
                double fuelPercent = fuel.getCurrent() / fuel.getTotal();

                pos = Point.of(10, mg.getHeight() - 40);
                drawPetFuel(mg, pos, (int) ((mg.getMiddle() - 20) * 0.25), 6, fuelPercent);
            }
        }

        Lockable target = hero.getLocalTarget();
        if (target != null && target.isValid()) {
            if (target instanceof Npc || target.getEntityInfo().isEnemy()) mg.setColor("enemies");
            else mg.setColor("allies");
            mg.setFont("mid");
            String name = target.getEntityInfo().getUsername();

            pos = Point.of(mg.getMiddle() + 10 + ((mg.getMiddle() - 20) >> 1), mg.getHeight() - 40);
            mg.drawString(name, pos, MapGraphics.Align.MID);

            pos = Point.of(mg.getMiddle() + 10, mg.getHeight() - 34);
            drawHealth(mg, target.getHealth(), pos, mg.getMiddle() - 20, 12, 0);
        }
    }

    protected void drawHealth(MapGraphics mg, Health health, Point pos, int width, int height, int margin) {
        boolean displayAmount = height >= 8 && hasDisplayFlag(DisplayFlag.HP_SHIELD_NUM);

        int totalMaxHealth = health.getMaxHp() + health.getHull();
        int hullWidth = totalMaxHealth == 0 ? 0 : (health.getHull() * width / totalMaxHealth);

        mg.setFont("small");
        mg.setColor("health", Color::darker);
        mg.drawRect(pos, true, width, height);
        mg.setColor("health");
        mg.drawRect(pos, true, hullWidth + (int) (health.hpPercent() * (width - hullWidth)), height);
        mg.setColor("nano_hull");
        mg.drawRect(pos, true, hullWidth, height);

        mg.setColor("text");
        if (displayAmount) {
            mg.drawString(HEALTH_FORMAT.format(health.getHull() + health.getHp()) + "/" + HEALTH_FORMAT.format(totalMaxHealth), pos.x() + width / 2, pos.y() + height - 2, MapGraphics.Align.MID);
        }

        if (health.getMaxShield() != 0) {
            mg.setColor("shield", Color::darker);
            Point shieldPos = Point.of(pos.x(), pos.y() + height + margin);

            mg.drawRect(shieldPos, true, width, height);
            mg.setColor("shield");
            mg.drawRect(shieldPos, true, (int) (health.shieldPercent() * width), height);
            mg.setColor("text");
            if (displayAmount) {
                mg.drawString(HEALTH_FORMAT.format(health.getShield()) + "/" + HEALTH_FORMAT.format(health.getMaxShield()), pos.x() + width / 2, pos.y() + height + height - 2, MapGraphics.Align.MID);
            }
        }
    }

    private void drawPetFuel(MapGraphics mg, Point pos, int width, int height, double fuelPercent) {
        mg.setColor("fuel", Color::darker);
        mg.drawRect(pos, true, width, height);
        mg.setColor("fuel");
        mg.drawRect(pos, true, (int) (fuelPercent * width), height);
    }

    private boolean hasDisplayFlag(DisplayFlag displayFlag) {
        return displayFlags.getValue().contains(displayFlag);
    }
}
