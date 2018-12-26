package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.objects.NpcType;

import java.util.HashMap;

import static com.github.manolo8.darkbot.Main.API;

public class Npc extends Ship {

    public static HashMap<Integer, NpcType> npcType;
    public static HashMap<Integer, HashMap<Integer, NpcType>> npcTypeByMap;

    static {

        npcType = new HashMap<>();
        npcTypeByMap = new HashMap<>();

        npcType.put(1, new NpcType("Streuner", 450));
        npcType.put(2, new NpcType("Lordakia", 450));
//        npcType.put(3, new NpcType("Devolarium", 536));
        npcType.put(4, new NpcType("Mordon", 500));
        npcType.put(16, new NpcType("Boss Mordon", 500));
        npcType.put(5, new NpcType("Sibelon", 530));
        npcType.put(6, new NpcType("Saimon", 500));
        npcType.put(7, new NpcType("Sibelonit", 575));
        npcType.put(9, new NpcType("Kristallin", 575));
        npcType.put(10, new NpcType("Kristallon", 600));
//        npcType.put(12, new NpcType("Protegit", 680));
        npcType.put(15, new NpcType("Boss Lordakia", 250));
        npcType.put(17, new NpcType("Boss Saimon", 250));
        npcType.put(22, new NpcType("Boss Kristallin", 550));
//        npcType.put(106, new NpcType("Hitac 2.0", 400));
        npcType.put(152, new NpcType("Recruit Streuner", 400));
        npcType.put(153, new NpcType("Aider Streuner", 400));
//        npcType.put(155, new NpcType("Blighted Gygerthrall", 580));
//        npcType.put(160, new NpcType("Blighted Kristallon.", 780));
        npcType.put(161, new NpcType("Blighted Kristallin", 550));


        HashMap<Integer, NpcType> ggAlpha = new HashMap<>();
        ggAlpha.put(1, new NpcType("Streuner α", 450, true));
        ggAlpha.put(2, new NpcType("Lordakia α", 450, true));
        ggAlpha.put(3, new NpcType("Devolarium α", 536));
        ggAlpha.put(4, new NpcType("Mordon α", 420, true));
        ggAlpha.put(5, new NpcType("Sibelon α", 530));
        ggAlpha.put(6, new NpcType("Saimon α", 500, true));
        ggAlpha.put(7, new NpcType("Sibelonit α", 575, true));
        ggAlpha.put(9, new NpcType("Kristallin α", 520, true));
        ggAlpha.put(10, new NpcType("Kristallon α", 550));
        ggAlpha.put(12, new NpcType("Protegit α", 680));

        HashMap<Integer, NpcType> ggBeta = new HashMap<>();
        ggBeta.put(1, new NpcType("Streuner β", 450, true));
        ggBeta.put(2, new NpcType("Lordakia β", 450, true));
        ggBeta.put(3, new NpcType("Devolarium β", 536));
        ggBeta.put(4, new NpcType("Mordon β", 450, true));
        ggBeta.put(5, new NpcType("Sibelon β", 530));
        ggBeta.put(6, new NpcType("Saimon β", 500, true));
        ggBeta.put(7, new NpcType("Sibelonit β", 575, true));
        ggBeta.put(9, new NpcType("Kristallin β", 620, true));
        ggBeta.put(10, new NpcType("Kristallon β", 600));
        ggBeta.put(12, new NpcType("Protegit β", 700));

        HashMap<Integer, NpcType> ggGamma = new HashMap<>();
        ggGamma.put(1, new NpcType("Streuner γ", 450, true));
        ggGamma.put(2, new NpcType("Lordakia γ", 450, true));
        ggGamma.put(3, new NpcType("Devolarium γ", 536));
        ggGamma.put(4, new NpcType("Mordon γ", 500, true));
        ggGamma.put(5, new NpcType("Sibelon γ", 530));
        ggGamma.put(6, new NpcType("Saimon γ", 500, true));
        ggGamma.put(7, new NpcType("Sibelonit γ", 575, true));
        ggGamma.put(9, new NpcType("Kristallin γ", 575, true));
        ggGamma.put(10, new NpcType("Kristallon γ", 620));
        ggGamma.put(12, new NpcType("Protegit γ", 720));


        npcTypeByMap.put(51, ggAlpha);
        npcTypeByMap.put(52, ggBeta);
        npcTypeByMap.put(53, ggGamma);

    }

    public NpcType type;

    public Npc(int id) {
        super(id);

        this.type = new NpcType("Unknown", 500);
    }

    @Override
    public void update() {
        super.update();

        int type = API.readMemoryInt(API.readMemoryLong(address + 192) + 80);

        int map = MapManager.id;

        NpcType temp = null;

        if (npcTypeByMap.containsKey(map)) {
            temp = npcTypeByMap.get(map).get(type);
        } else {
            temp = npcType.get(type);
        }


        if (temp != null) {
            this.type = temp;
        } else {
            this.type.name = "Unknown #" + type;
        }
    }
}
