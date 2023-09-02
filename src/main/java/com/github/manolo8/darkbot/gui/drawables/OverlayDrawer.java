package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.core.objects.facades.BoosterProxy;
import com.github.manolo8.darkbot.core.objects.group.GroupMember;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.types.DisplayFlag;
import eu.darkbot.api.extensions.Draw;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.other.Point;
import eu.darkbot.api.game.stats.Stats;
import eu.darkbot.api.managers.BoosterAPI;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.GroupAPI;
import eu.darkbot.api.managers.RepairAPI;
import eu.darkbot.api.managers.StatsAPI;
import eu.darkbot.util.TimeUtils;

import java.awt.font.TextAttribute;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Feature(name = "Overlay Drawer", description = "Draws overlays (eg: stats, boosters, or group info)")
@Draw(value = Draw.Stage.OVERLAY, attach = Draw.Attach.REPLACE)
public class OverlayDrawer implements Drawable {
    private static final DecimalFormat STAT_FORMAT = new DecimalFormat("###,###,###");

    private final BoosterAPI boosters;
    private final GroupAPI group;
    private final StatsAPI stats;
    private final RepairAPI repair;

    private final StatsAPI.Stat runtime;

    private final ConfigSetting<Integer> maxDeaths;

    public OverlayDrawer(BoosterAPI boosters, GroupAPI group, StatsAPI stats, RepairAPI repair, ConfigAPI config) {
        this.boosters = boosters;
        this.group = group;
        this.stats = stats;
        this.runtime = stats.getStat(Stats.Bot.RUNTIME);
        this.repair = repair;

        this.maxDeaths = config.requireConfig("general.safety.max_deaths");
    }

    @Override
    public void onDraw(MapGraphics mg) {
        drawStats(mg);
        if (!drawGroup(mg)) drawBoosters(mg);
    }

    private boolean drawGroup(MapGraphics mg) {
        if (!mg.hasDisplayFlag(DisplayFlag.GROUP_AREA) || !group.hasGroup()) return false;

        boolean hideNames = !mg.hasDisplayFlag(DisplayFlag.GROUP_NAMES);

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

                    InfosDrawer.drawHealth(mg, member.getMemberInfo(), Point.of(x, y + 18), w / 2 - 3, 4, 2);

                    if (member.getTargetInfo().getShipType() != 0)
                        InfosDrawer.drawHealth(mg, member.getTargetInfo(), Point.of(x + (w / 2d) + 3, y + 18), w / 2 - 3, 4, 2);

                },
                member -> {
                    String text = ((GroupMember) member).getDisplayText(hideNames);
                    return Math.min(mg.getGraphics2D().getFontMetrics().stringWidth(text), 200);
                }, group.getMembers());

        return true;
    }

    private void drawBoosters(MapGraphics mg) {
        if (!mg.hasDisplayFlag(DisplayFlag.BOOSTER_AREA)) return;

        Stream<? extends BoosterAPI.Booster> boosters = this.boosters.getBoosters().stream().filter(b -> b.getAmount() > 0);
        if (mg.hasDisplayFlag(DisplayFlag.SORT_BOOSTERS))
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
        if (mg.hasDisplayFlag(DisplayFlag.STATS_AREA))
            drawBackgroundedText(mg,
                    "cre/h " + toEarnedPerHour(stats.getEarnedCredits()),
                    "uri/h " + toEarnedPerHour(stats.getEarnedUridium()),
                    "exp/h " + toEarnedPerHour(stats.getEarnedExperience()),
                    "hon/h " + toEarnedPerHour(stats.getEarnedHonor()),
                    "cargo " + stats.getCargo() + '/' + stats.getMaxCargo(),
                    "death " + repair.getDeathAmount() + '/' + (maxDeaths.getValue() > -1 ? maxDeaths.getValue() : "∞"));

    }

    private String toEarnedPerHour(double value) {
        // Lose millisecond precision, in hopes of better double precision, at least always 1s runtime
        long seconds = Math.max((long) (runtime.getEarned() / TimeUtils.SECOND), 1L);
        return STAT_FORMAT.format(value / (seconds / 3600d));
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
        if (toRender.isEmpty()) return;
        mg.setFont("small");

        int width = toRender.stream().mapToInt(widthGetter).max().orElse(0) + 8;
        int height = toRender.size() * lineHeight + 4;
        int top = mg.getHeight() / 2 - height / 2;
        int left = align == MapGraphics.StringAlign.RIGHT ? mg.getWidth() - width : 0;

        mg.setColor("texts_background");
        mg.drawRect((double) left, top, width, height, true);
        mg.setColor("text");

        for (T render : toRender) {
            renderer.render(left + 4, top, width - 8, render);
            top += lineHeight;
        }
    }

    private interface Renderer<T> {
        void render(int x, int y, int w, T object);
    }
}
