package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.core.manager.MapManager;

public class ConfigEntity {

    public static ConfigEntity INSTANCE;

    private final Config config;

    public ConfigEntity(Config config) {
        this.config = config;
        INSTANCE = this;
    }

    public NpcInfo getOrCreateNpcInfo(String name) {

        int mapId = MapManager.id;

        NpcInfo info = config.LOOT.NPC_INFOS.get(name);

        if (info == null) {
            info = new NpcInfo();

            info.radius = 400;
            info.mapList.add(mapId);

            if (!name.equals("ERROR") && !name.isEmpty()) {
                config.LOOT.NPC_INFOS.put(name, info);

                config.addedNpc.send(name);

                config.changed = true;
            }

        } else {

            if (info.mapList.add(mapId)) {
                config.changed = true;
            }

        }

        return info;
    }

    public BoxInfo getOrCreateBoxInfo(String name) {

        BoxInfo info = config.COLLECT.BOX_INFOS.get(name);

        if (info == null) {
            info = new BoxInfo();

            if (!name.equals("ERROR") && !name.isEmpty()) {
                config.COLLECT.BOX_INFOS.put(name, info);

                config.addedBox.send(name);

                config.changed = true;
            }
        }

        return info;
    }
}
