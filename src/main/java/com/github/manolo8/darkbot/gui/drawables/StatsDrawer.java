package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag;
import com.github.manolo8.darkbot.core.objects.facades.BoosterProxy;
import com.github.manolo8.darkbot.core.objects.group.GroupMember;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.other.Point;
import eu.darkbot.api.managers.BoosterAPI;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.GroupAPI;
import eu.darkbot.api.managers.RepairAPI;
import eu.darkbot.api.managers.StatsAPI;

import java.awt.font.TextAttribute;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Feature(name = "Stats Drawer", description = "Draws statistics")
public class StatsDrawer extends InfosDrawer {
    private static final DecimalFormat STAT_FORMAT = new DecimalFormat("###,###,###");

    private final BoosterAPI boosters;
    private final GroupAPI group;
    private final StatsAPI stats;
    private final RepairAPI repair;

    private final ConfigSetting<Integer> maxDeaths;
    private final ConfigSetting<Set<DisplayFlag>> displayFlags;

    public StatsDrawer(PluginAPI api, BoosterAPI boosters, GroupAPI group, StatsAPI stats, RepairAPI repair, ConfigAPI config) {
        super(api);

        this.boosters = boosters;
        this.group = group;
        this.stats = stats;
        this.repair = repair;

        this.maxDeaths = config.requireConfig("general.safety.max_deaths");
        this.displayFlags = config.requireConfig("bot_settings.map_display.toggle");
    }

    @Override
    public void onDraw(MapGraphics mg) {
        drawStats(mg);
        if (!drawGroup(mg)) drawBoosters(mg);
    }

    private boolean drawGroup(MapGraphics mg) {
        if (!hasDisplayFlag(DisplayFlag.GROUP_AREA) || !group.hasGroup()) return false;

        boolean hideNames = !hasDisplayFlag(DisplayFlag.GROUP_NAMES);

        drawBackgrounded(mg, 28, MapGraphics.StringAlign.RIGHT,
                (x, y, w, member) -> {

                    Map<TextAttribute, Object> attrs = new HashMap<>();
                    attrs.put(TextAttribute.WEIGHT, member.isLeader() ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);
                    attrs.put(TextAttribute.STRIKETHROUGH, member.isDead() ? TextAttribute.STRIKETHROUGH_ON : false);
                    attrs.put(TextAttribute.UNDERLINE, member.isLocked() ? TextAttribute.UNDERLINE_ON : -1);

                    mg.setFont(mg.getFont("small").deriveFont(attrs));
                    mg.setColor(member.isCloaked() ? mg.getColor("text").darker() : mg.getColor("text"));

                    String text = ((GroupMember) member).getDisplayText(hideNames);
                    mg.getGraphics2D().drawString(text, x, y + 14);

                    drawHealth(mg, member.getMemberInfo(), Point.of(x, y + 18), w / 2 - 3, 4, 2);

                    if (member.getTargetInfo().getShipType() != 0)
                        drawHealth(mg, member.getTargetInfo(), Point.of(x + (w / 2d) + 3, y + 18), w / 2 - 3, 4, 2);

                },
                member -> {
                    String text = ((GroupMember) member).getDisplayText(hideNames);
                    return Math.min(mg.getGraphics2D().getFontMetrics().stringWidth(text), 200);
                }, group.getMembers());

        return true;
    }

    private void drawBoosters(MapGraphics mg) {
        if (!hasDisplayFlag(DisplayFlag.BOOSTER_AREA)) return;

        Stream<? extends BoosterAPI.Booster> boosters = this.boosters.getBoosters().stream().filter(b -> b.getAmount() > 0);
        if (hasDisplayFlag(DisplayFlag.SORT_BOOSTERS))
            boosters = boosters.sorted(Comparator.comparingDouble(b -> -b.getRemainingTime()));

        drawBackgrounded(mg, 15, MapGraphics.StringAlign.RIGHT,
                (x, y, w, booster) -> {
                    mg.setColor(booster.getColor());
                    mg.getGraphics2D().drawString(((BoosterProxy.Booster) booster).toSimpleString(), x, y + 14);
                },
                b -> mg.getGraphics2D().getFontMetrics().stringWidth(((BoosterProxy.Booster) b).toSimpleString()),
                boosters.collect(Collectors.toList()));
    }

    private void drawStats(MapGraphics mg) {
        if (hasDisplayFlag(DisplayFlag.STATS_AREA))
            drawBackgroundedText(mg,
                    "cre/h " + STAT_FORMAT.format(stats.getEarnedCredits()),
                    "uri/h " + STAT_FORMAT.format(stats.getEarnedUridium()),
                    "exp/h " + STAT_FORMAT.format(stats.getEarnedExperience()),
                    "hon/h " + STAT_FORMAT.format(stats.getEarnedHonor()),
                    "cargo " + stats.getCargo() + "/" + stats.getMaxCargo(),
                    "death " + repair.getDeathAmount() + '/' + (maxDeaths.getValue() > -1 ? maxDeaths.getValue() : "∞"));

    }

    private void drawBackgroundedText(MapGraphics mg, String... texts) {
        this.drawBackgrounded(mg, 15, MapGraphics.StringAlign.LEFT,
                (x, y, h, str) -> mg.getGraphics2D().drawString(str, x, y + 14),
                s -> mg.getGraphics2D().getFontMetrics().stringWidth(s),
                Arrays.asList(texts));
    }

    private <T> void drawBackgrounded(MapGraphics mg, int lineHeight, MapGraphics.StringAlign align,
                                      Renderer<T> renderer,
                                      ToIntFunction<T> widthGetter,
                                      Collection<T> toRender) {
        if (toRender.size() == 0) return;
        mg.setFont("small");

        int width = toRender.stream().mapToInt(widthGetter).max().orElse(0) + 8;
        int height = toRender.size() * lineHeight + 4;
        int top = mg.getHeight() / 2 - height / 2;
        int left = align == MapGraphics.StringAlign.RIGHT ? mg.getWidth() - width : 0;

        mg.setColor("texts_background");
        mg.drawRect(left, top, width, height, true);
        mg.setColor("text");

        for (T render : toRender) {
            renderer.render(left + 4, top, width - 8, render);
            top += lineHeight;
        }
    }

    private boolean hasDisplayFlag(DisplayFlag displayFlag) {
        return displayFlags.getValue().contains(displayFlag);
    }

    private interface Renderer<T> {
        void render(int x, int y, int w, T object);
    }
}
