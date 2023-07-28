package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.config.NpcExtraFlag;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.types.NpcFlag;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.NpcFlags;
import eu.darkbot.api.extensions.PluginInfo;
import eu.darkbot.api.managers.I18nAPI;
import eu.darkbot.api.utils.Inject;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

public class NpcExtraHandler extends FeatureHandler<NpcFlags<?>> {

    private static final Class<?>[] NATIVE = new Class[]{DefaultNpcFlag.class};

    @Override
    public Class<?>[] getNativeFeatures() {
        return NATIVE;
    }

    private I18nAPI i18n;

    @Inject
    public void setI18n(I18nAPI i18n) {
        this.i18n = i18n;
    }

    @Override
    public void update(Stream<FeatureDefinition<NpcFlags<?>>> flags) {
        NpcInfo.setNpcFlags(
                flags.flatMap(fd -> featureRegistry.getFeature(fd)
                        .map(flag -> tryGetNpcFlags(fd, flag))
                        .orElse(Stream.empty()))
                , false);
    }

    private Stream<NpcExtraFlag> tryGetNpcFlags(FeatureDefinition<NpcFlags<?>> fd, NpcFlags<?> instance) {
        try {
            return getNpcFlags(fd.getPluginInfo(), instance);
        } catch (Exception e) {
            fd.getIssues().addFailure("bot.issue.feature.failed_to_load", IssueHandler.createDescription(e));
            return Stream.empty();
        }
    }

    private <T extends Enum<T>> Stream<NpcExtraFlag> getNpcFlags(PluginInfo namespace, NpcFlags<?> npcFlags) {
        Type[] types = ReflectionUtils.findGenericParameters(npcFlags.getClass(), NpcFlags.class);
        if (types == null || types.length == 0 || !(types[0] instanceof Class))
            throw new IllegalArgumentException("NpcFlags must include a class in the generic argument: "
                    + npcFlags.getClass().getCanonicalName());

        @SuppressWarnings("unchecked")
        Class<T> flagEnum = (Class<T>) types[0];

        T[] constants = flagEnum.getEnumConstants();
        if (constants == null) throw new IllegalArgumentException(
                "NpcFlags must be an enum: " + npcFlags.getClass().getCanonicalName());

        Configuration config = flagEnum.getAnnotation(Configuration.class);
        if (config == null) throw new IllegalArgumentException(
                "NpcFlags type must be annotated with @Configuration in " + flagEnum.getCanonicalName());

        return Arrays.stream(constants)
                .map(flag -> createFlag(i18n, namespace, config.value(), flag));
    }

    public <T extends Enum<T>> NpcExtraFlag createFlag(I18nAPI i18n, PluginInfo namespace,
                                                       String base, T npcFlag) {
        String name = npcFlag.name().toLowerCase(Locale.ROOT);
        String id = NpcInfo.getId(npcFlag);
        return new NpcExtraFlagImpl(
                id,
                i18n.getOrDefault(namespace, base + "." + name + ".short", name),
                i18n.getOrDefault(namespace, base + "." + name, name),
                i18n.getOrDefault(namespace, base + "." + name + ".desc", name)
        );
    }

    private static class NpcExtraFlagImpl implements NpcExtraFlag {
        private final String id, shortName, name, desc;

        public NpcExtraFlagImpl(String id, String shortName, String name, String desc) {
            this.id = id;
            this.shortName = shortName;
            this.name = name;
            this.desc = desc;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getShortName() {
            return shortName;
        }

        @Override
        public String getDescription() {
            return desc;
        }
    }

    @Feature(name = "Default npc flags", description = "Provides default npc extra flags")
    public static class DefaultNpcFlag implements NpcFlags<NpcFlag> {}

}
