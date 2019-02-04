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

        NpcInfo info = config.npcInfos.get(name);

        if (info == null) {
            info = new NpcInfo();

            info.radius = 400;
            info.mapList.add(mapId);

            if (!name.equals("ERROR") && !name.isEmpty()) {
                config.npcInfos.put(name, info);

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

        BoxInfo info = config.boxInfos.get(name);

        if (info == null) {
            info = new BoxInfo();

            if (!name.equals("ERROR") && !name.isEmpty()) {
                config.boxInfos.put(name, info);

                config.addedBox.send(name);

                config.changed = true;
            }
        }

        return info;
    }
}
