package com.github.manolo8.darkbot.config.actions.parser;

import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.conditions.AfterCondition;
import com.github.manolo8.darkbot.config.actions.conditions.AllCondition;
import com.github.manolo8.darkbot.config.actions.conditions.AnyCondition;
import com.github.manolo8.darkbot.config.actions.conditions.EqualCondition;
import com.github.manolo8.darkbot.config.actions.conditions.HasEffectCondition;
import com.github.manolo8.darkbot.config.actions.conditions.HasFormationCondition;
import com.github.manolo8.darkbot.config.actions.conditions.NoneCondition;
import com.github.manolo8.darkbot.config.actions.conditions.NumericalCondition;
import com.github.manolo8.darkbot.config.actions.conditions.OneCondition;
import com.github.manolo8.darkbot.config.actions.conditions.TargetTypeCondition;
import com.github.manolo8.darkbot.config.actions.conditions.UntilCondition;
import com.github.manolo8.darkbot.config.actions.values.BooleanConstant;
import com.github.manolo8.darkbot.config.actions.values.DistanceValue;
import com.github.manolo8.darkbot.config.actions.values.HealthTypeValue;
import com.github.manolo8.darkbot.config.actions.values.HealthValue;
import com.github.manolo8.darkbot.config.actions.values.HeroMap;
import com.github.manolo8.darkbot.config.actions.values.HeroValue;
import com.github.manolo8.darkbot.config.actions.values.LocationConstant;
import com.github.manolo8.darkbot.config.actions.values.MapConstant;
import com.github.manolo8.darkbot.config.actions.values.NumberConstant;
import com.github.manolo8.darkbot.config.actions.values.PercentConstant;
import com.github.manolo8.darkbot.config.actions.values.ShipLocationValue;
import com.github.manolo8.darkbot.config.actions.values.ShipName;
import com.github.manolo8.darkbot.config.actions.values.StatTypeValue;
import com.github.manolo8.darkbot.config.actions.values.StringConstant;
import com.github.manolo8.darkbot.config.actions.values.TargetValue;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Values {
    private static final List<Class<? extends Value<?>>> AVAILABLE_VALUES =
            Arrays.asList(
                    // Combination conditions
                    AllCondition.class,
                    AnyCondition.class,
                    OneCondition.class,
                    NoneCondition.class,
                    // Comparison conditions
                    NumericalCondition.class,
                    EqualCondition.class,
                    HasEffectCondition.class,
                    HasFormationCondition.class,
                    TargetTypeCondition.class,
                    // Stateful conditions
                    AfterCondition.class,
                    UntilCondition.class,
                    // Values
                    DistanceValue.class,
                    ShipLocationValue.class,
                    ShipName.class,
                    HeroMap.class,
                    StatTypeValue.class,
                    HealthTypeValue.class,
                    HealthValue.class,
                    HeroValue.class,
                    TargetValue.class,
                    // Constants
                    NumberConstant.class,
                    PercentConstant.class,
                    BooleanConstant.class,
                    LocationConstant.class,
                    MapConstant.class,
                    StringConstant.class);

    private static final Map<String, Meta<?>> VALUES = buildMetadata();

    private static Map<String, Meta<?>> buildMetadata() {
        Map<String, Meta<?>> metadata = new HashMap<>();
        for (Class<? extends Value<?>> value : AVAILABLE_VALUES) {
            @SuppressWarnings("unchecked")
            Meta<?> vm = new Meta(value);
            metadata.put(vm.getName(), vm);
        }
        return metadata;
    }

    public static <T extends Value<?>> Meta<T> getMeta(Class<T> type) throws SyntaxException {
        for (Meta<?> value : VALUES.values()) {
            if (value.clazz == type) //noinspection unchecked
                return (Meta<T>) value;
        }
        throw new SyntaxException("Error: failed to find value meta for " + type.getSimpleName(), null);
    }

    public static <T> Meta<T> getMeta(String name, String ex, Class<T> type) throws SyntaxException {
        //noinspection unchecked
        Meta<T> meta = (Meta<T>) VALUES.get(name);

        List<Meta<?>> params = VALUES.values().stream()
                .filter(v -> type.isAssignableFrom(v.type))
                .collect(Collectors.toList());

        if (meta == null)
            throw new SyntaxException("Unknown value type '" + name + "'", ex, params);
        if (!type.isAssignableFrom(meta.type))
            throw new SyntaxException("Invalid type, expected '" + type.getSimpleName() + "'", ex, params);

        return meta;
    }

    public static class Meta<T> {
        public final ValueData valueData;
        public final Class<? extends Value<T>> clazz;
        public final Class<T> type;
        public final Param[] params;

        public Meta(Class<? extends Value<T>> clazz) {
            this.valueData = clazz.getAnnotation(ValueData.class);
            if (valueData == null)
                throw new RuntimeException("Available value " + clazz.getName() + " has no value data!");
            this.clazz = clazz;
            Type[] t = ReflectionUtils.findGenericParameters(clazz, Value.class);
            if (t == null || !(t[0] instanceof Class))
                throw new RuntimeException("Value types must be findable classes. " + clazz.getName() + " has no class!");
            //noinspection unchecked
            this.type = (Class<T>) t[0];
            this.params = Parser.class.isAssignableFrom(clazz) ? null :
                    Arrays.stream(clazz.getDeclaredFields())
                            .filter(f -> !Modifier.isTransient(f.getModifiers()))
                            .map(Param::new)
                            .toArray(Param[]::new);
        }

        public String getName() {
            return valueData.name();
        }

        public String getDescription() {
            return valueData.description();
        }

        public String getExample() {
            return valueData.example();
        }
    }

    public static class Param {
        public final Class<?> type;
        public final Field field;

        public Param(Field field) {
            Type[] types = ReflectionUtils.getTypes(field.getGenericType(), Value.class);
            if (types == null) types = ReflectionUtils.getTypes(field.getType(), Value.class);

            if (types == null || !(types[0] instanceof Class))
                throw new RuntimeException("Error: Failed to find type info for " + field);

            this.type = (Class<?>) types[0];
            this.field = field;
        }
    }
    
}
