package com.github.manolo8.darkbot.extensions.features.decorators;

import com.github.manolo8.darkbot.config.NpcExtraFlag;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.core.itf.InstructionProvider;
import com.github.manolo8.darkbot.core.itf.NpcExtraProvider;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NpcExtraDecorator extends FeatureDecorator<NpcExtraProvider> {

    @Override
    protected void load(FeatureDefinition<NpcExtraProvider> fd, NpcExtraProvider obj) {
        for (NpcExtraFlag value : obj.values()) {
            NpcInfo.NPC_FLAGS.put(value.getId(), value);
        }
    }

    @Override
    protected void unload(NpcExtraProvider obj) {
        for (NpcExtraFlag value : obj.values()) {
            NpcInfo.NPC_FLAGS.remove(value.getId());
        }
    }

}
