package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.types.Col;
import com.github.manolo8.darkbot.config.types.Option;

import java.awt.*;
import java.util.stream.IntStream;

public class ColorScheme {

    public @Option @Col(alpha = false) Color BACKGROUND = new Color(0x263238);
    public @Option @Col(alpha = false) Color RADIATION = new Color(0x372638);
    public @Option Color TEXT = new Color(0xF2F2F2);
    public @Option Color TEXT_DARK = new Color(0xBBBBBB);
    public @Option Color GOING = new Color(0x8F9BFF);
    public @Option Color PORTALS = new Color(0xAEAEAE);
    public @Option Color HERO = new Color(0x22CC22);
    public @Option @Col(alpha = false) Color TRAIL_BASE = new Color(0xE0E0E0);
    public @Option Color FUEL = new Color(0xF2F2F2);
    public @Option Color BOXES = new Color(0xBBB830);
    public @Option Color MINES = new Color(0xFF8000);
    public @Option Color ALLIES = new Color(0x29B6F6);
    public @Option Color ENEMIES = new Color(0xd50000);
    public @Option Color NPCS = new Color(0xAA4040);
    public @Option Color GROUP_MEMBER = new Color(0xFFD700);

    public @Option Color LOW_RELAYS = new Color(0x00D54B);
    public @Option Color SPACE_BALLS = new Color(0x00D595);
    public @Option Color OTHER_ENTITIES = new Color(0x1647A1);

    public @Option Color TARGET = NPCS.darker();
    public @Option Color PET = new Color(0x004c8c);
    public @Option Color PET_IN = new Color(0xc56000);
    public @Option Color HEALTH = new Color(0x388e3c);
    public @Option Color NANO_HULL = new Color(0xD0D024);
    public @Option Color SHIELD = new Color(0x0288d1);
    public @Option Color METEROID = new Color(0xAAAAAA);
    public @Option Color PING = new Color(0x2000ff00, true);
    public @Option Color PING_BORDER = new Color(0x8000ff00, true);
    public @Option Color BARRIER = new Color(0x20ffffff, true);
    public @Option Color BARRIER_BORDER = new Color(0x80ffffff, true);
    public @Option Color NO_CLOACK = new Color(0x2018a0ff, true);
    public @Option Color PREFER = new Color(0x2000ff80, true);
    public @Option Color AVOID = new Color(0x20ff0000, true);
    public @Option Color SAFETY = new Color(0x601080ff, true);

    public @Option Color BASES = new Color(0x00ff80);
    public @Option Color BASE_SPOTS = new Color(0x2000ff80, true);
    public @Option Color UNKNOWN = new Color(0x7C05D1);
    public @Option Color TEXTS_BACKGROUND = new Color(0x80263238, true);

    public @Option Color ACTION_BUTTON = new Color(0xa0ffffff, true);
    public @Option Color DARKEN_BACK = new Color(0x60000000, true);

    public @Option ZoneEditor ZONE_EDITOR = new ZoneEditor();
    public static class ZoneEditor {
        public @Option Color LINES = new Color(128, 128, 128, 128);
        public @Option Color HOVERING = new Color(0, 150, 200);
        public @Option Color SELECTING = new Color(0, 128, 255);
        public @Option Color ZONE = new Color(0, 255, 128, 64);
    }

    public @Option SafetyEditor SAFETY_EDITOR = new SafetyEditor();
    public static class SafetyEditor {
        public @Option Color ZONE_HIGHLIGHT = new Color(0, 255, 128, 96);
        public @Option Color ZONE_SELECTED = new Color(0, 255, 128, 128);
        public @Option Color ZONE_SOLID = new Color(0, 255, 128);
    }

    public @Option Fonts FONTS = new Fonts();
    public static class Fonts {
        public @Option Font BIG = new Font("Consolas", Font.PLAIN, 32);
        public @Option Font MID = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
        public @Option Font SMALL = new Font("Consolas", Font.PLAIN, 13);
        public @Option Font TINY = new Font(Font.SANS_SERIF, Font.PLAIN, 9);
    }

    private transient Color[] TRAIL = null;

    public Color[] getTrail() {
        if (TRAIL == null || (TRAIL[0].getRGB() & 0x00FFFFFF) != (TRAIL_BASE.getRGB() & 0x00FFFFFF))
            TRAIL = IntStream.rangeClosed(1, 255)
                    .mapToObj(i -> new Color(TRAIL_BASE.getRed(), TRAIL_BASE.getGreen(), TRAIL_BASE.getBlue(), i))
                    .toArray(Color[]::new);
        return TRAIL;
    }

}
