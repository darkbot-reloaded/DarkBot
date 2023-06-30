package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.types.DisplayFlag;
import eu.darkbot.api.extensions.Draw;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.other.GameMap;
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
import java.awt.geom.Arc2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

@Feature(name = "Infos Drawer", description = "Draws info about bot state, current map, hero & target health etc.")
@Draw(value = Draw.Stage.INFO_AND_HEALTH, attach = Draw.Attach.REPLACE)
public class InfosDrawer implements Drawable {

    private static final NumberFormat HEALTH_FORMAT;
    private static final NumberFormat TWO_PLACES_FORMAT = new DecimalFormat("0.00");

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

    private final ConfigSetting<Boolean> resetRefresh;
    private final ConfigSetting<Integer> refreshTime;

    private Arc2D countDown;

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

        this.resetRefresh = config.requireConfig("miscellaneous.reset_refresh");
        this.refreshTime = config.requireConfig("miscellaneous.refresh_time");
    }

    @Override
    public void onDraw(MapGraphics mg) {
        drawInfos(mg);
        drawMap(mg);

        drawHealth(mg);
    }

    private String getSysInfo() {
        return (Runtime.getRuntime().totalMemory() >> 20) + "MB heap, " +
                main.facadeManager.stats.getMemory() + '|' +
                Main.API.getMemoryUsage() + "MB ram, " +
                TWO_PLACES_FORMAT.format(Main.API.getCpuUsage()) + " cpu";
    }

    private String getTickInfo() {
        return TWO_PLACES_FORMAT.format(main.getTickTime()) + "tick " + stats.getPing() + "ms ping";
    }

    private void drawInfos(MapGraphics mg) {
        mg.setColor("text_dark");
        
        mg.setFont("small");
        String status = i18n.get((main.isRunning() ? "gui.map.running" : "gui.map.waiting"),
                Time.toString(stats.getRunningTime().toMillis()));

        mg.drawString(mg.getWidthMiddle(), mg.getHeightMiddle() + 35, status, MapGraphics.StringAlign.MID);

        mg.setFont("small");
        String info = Main.API.getRefreshCount() + " - "
                + (main.isRunning() || !resetRefresh.getValue()
                ? Time.toString(System.currentTimeMillis() - main.lastRefresh) : "00")
                + "/" + Time.toString(refreshTime.getValue() * 60 * 1000L);
        mg.drawString(23, 12, info, MapGraphics.StringAlign.LEFT);

        if (main.getModule() != null) {
            String s = (main.isRunning() && main.repairManager.isDestroyed())
                    ? main.repairManager.getStatus()
                    : (main.tickingModule ? main.getModule().getStatus() : main.getModule().getStoppedStatus());
            if (s != null) {
                int i = 12;
                for (String line : s.split("\n"))
                    mg.drawString(7, i += 14, line, MapGraphics.StringAlign.LEFT);
            }
        }

        mg.drawString(mg.getWidth() - 5, 12, "SID: " + main.backpage.sidStatus(), MapGraphics.StringAlign.RIGHT);
    }

    public void drawMap(MapGraphics mg) {
        mg.setColor("text_dark");
        mg.setFont("big");

        String name = hero.getMap().getId() == -1 ? I18n.get("gui.map.loading") : hero.getMap().getName();

        GameMap next = main.hero.nextMap;
        GameMap nextCpuMap = main.hero.nextCpuMap;
        if (next.getId() != -1 && next != hero.getMap()) // change with api method later
            name += "→" + next.getName();
        else if (nextCpuMap.getId() != -1 && nextCpuMap != hero.getMap() && main.hero.nextCpuMapDuration != 0) {
            double countdown = (main.hero.nextCpuMapDuration - System.currentTimeMillis()) / 1000.0 / 10.0;
            if (countdown > 0) {
                name += "→" + nextCpuMap.getName();

                if (countDown == null) countDown = new Arc2D.Double();
                countDown.setArcByCenter(mg.getWidthMiddle(), mg.getHeightMiddle() - 50,
                        mg.getGraphics2D().getFontMetrics().getHeight() / 2.2,
                        90, 360 * countdown, Arc2D.PIE);

                mg.getGraphics2D().setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                mg.getGraphics2D().fill(countDown);
                mg.getGraphics2D().setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            }
        }

        mg.drawString(mg.getWidthMiddle(), mg.getHeightMiddle() - 5, name, MapGraphics.StringAlign.MID);
    }

    private void drawHealth(MapGraphics mg) {
        mg.setColor("text");
        mg.setFont("mid");

        if (mg.hasDisplayFlag(DisplayFlag.HERO_NAME))
            mg.drawString(10 + (mg.getWidthMiddle() - 20) / 2.0, mg.getHeight() - 40,
                    hero.getEntityInfo().getUsername(), MapGraphics.StringAlign.MID);

        Point pos = Point.of(10, mg.getHeight() - 34);
        drawHealth(mg, hero.getHealth(), pos, mg.getWidthMiddle() - 20, 12, 0);

        if (pet.isValid() && mg.hasDisplayFlag(DisplayFlag.SHOW_PET)) {
            pos = Point.of(10, mg.getHeight() - 52);
            double petHealthWidth = ((mg.getWidthMiddle() - 20) * 0.25);

            drawHealth(mg, pet.getHealth(), pos, petHealthWidth, 6, 0);

            mg.setFont("small");
            if (mg.hasDisplayFlag(DisplayFlag.HERO_NAME))
                mg.drawString(10 + petHealthWidth / 2.0, mg.getHeight() - 56,
                        pet.getEntityInfo().getUsername(), MapGraphics.StringAlign.MID);

            Lockable petTarget = pet.getTargetAs(Lockable.class);
            if (petTarget != null && petTarget.isValid()) {
                pos = Point.of(mg.getWidthMiddle() - petHealthWidth - 10, mg.getHeight() - 52);

                drawHealth(mg, petTarget.getHealth(), pos, petHealthWidth, 6, 0);

                if (petTarget instanceof Npc || petTarget.getEntityInfo().isEnemy()) mg.setColor("enemies");
                else mg.setColor("allies");
                mg.drawString(mg.getWidthMiddle() - 10 - petHealthWidth / 2.0, mg.getHeight() - 56,
                        petTarget.getEntityInfo().getUsername(), MapGraphics.StringAlign.MID);
            }

            PetAPI.PetStat fuel = pet.getStat(PetAPI.Stat.FUEL);
            if (fuel != null) {
                double fuelPercent = fuel.getCurrent() / fuel.getTotal();

                pos = Point.of(10, mg.getHeight() - 40);
                drawPetFuel(mg, pos, ((mg.getWidthMiddle() - 20) * 0.25), 6, fuelPercent);
            }
        }

        Lockable target = hero.getLocalTarget();

        if (target != null && target.isValid()) {
            if (target instanceof Npc || target.getEntityInfo().isEnemy()) mg.setColor("enemies");
            else mg.setColor("allies");
            mg.setFont("mid");
            String name = target.getEntityInfo().getUsername();

            pos = Point.of(mg.getWidthMiddle() + 10 + ((mg.getWidthMiddle() - 20) >> 1), mg.getHeight() - 40);
            mg.drawString(pos, name, MapGraphics.StringAlign.MID);

            pos = Point.of(mg.getWidthMiddle() + 10, mg.getHeight() - 34);
            drawHealth(mg, target.getHealth(), pos, mg.getWidthMiddle() - 20, 12, 0);
        }
    }

    public static void drawHealth(MapGraphics mg, Health health, Point pos, double width, double height, int margin) {
        boolean displayAmount = height >= 8 && mg.hasDisplayFlag(DisplayFlag.HP_SHIELD_NUM);

        int totalMaxHealth = health.getMaxHp() + health.getHull();
        double hullWidth = totalMaxHealth == 0 ? 0 : (health.getHull() * width / totalMaxHealth);

        mg.setFont("small");
        mg.setColor(mg.getColor("health").darker());
        mg.drawRect(pos, width, height, true);
        mg.setColor("health");
        mg.drawRect(pos, hullWidth + (health.hpPercent() * (width - hullWidth)), height, true);
        mg.setColor("nano_hull");
        mg.drawRect(pos, hullWidth, height, true);

        mg.setColor("text");
        if (displayAmount) {
            mg.drawString(pos.getX() + width / 2.0, pos.getY() + height - 2,
                    HEALTH_FORMAT.format(health.getHull() + health.getHp())
                            + "/" + HEALTH_FORMAT.format(health.getMaxHp()), MapGraphics.StringAlign.MID);
        }

        if (health.getMaxShield() != 0) {
            mg.setColor(mg.getColor("shield").darker());
            Point shieldPos = Point.of(pos.getX(), pos.getY() + height + margin);

            mg.drawRect(shieldPos, width, height, true);
            mg.setColor("shield");
            mg.drawRect(shieldPos, (health.shieldPercent() * width), height, true);
            mg.setColor("text");
            if (displayAmount) {
                mg.drawString(pos.getX() + width / 2.0, pos.getY() + height + height - 2,
                        HEALTH_FORMAT.format(health.getShield())
                                + "/" + HEALTH_FORMAT.format(health.getMaxShield()), MapGraphics.StringAlign.MID);
            }
        }
    }

    private void drawPetFuel(MapGraphics mg, Point pos, double width, double height, double fuelPercent) {
        mg.setColor(mg.getColor("fuel").darker());
        mg.drawRect(pos, width, height, true);
        mg.setColor("fuel");
        mg.drawRect(pos, (fuelPercent * width), height, true);
    }
}
