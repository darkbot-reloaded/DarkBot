package com.github.manolo8.darkbot.config.actions;

import com.github.manolo8.darkbot.config.actions.conditions.AllCondition;
import com.github.manolo8.darkbot.config.actions.conditions.AnyCondition;
import com.github.manolo8.darkbot.config.actions.conditions.EqualCondition;
import com.github.manolo8.darkbot.config.actions.conditions.NoneCondition;
import com.github.manolo8.darkbot.config.actions.conditions.NumericalCondition;
import com.github.manolo8.darkbot.config.actions.conditions.OneCondition;
import com.github.manolo8.darkbot.config.actions.values.BooleanConstant;
import com.github.manolo8.darkbot.config.actions.values.DistanceValue;
import com.github.manolo8.darkbot.config.actions.values.HealthTypeValue;
import com.github.manolo8.darkbot.config.actions.values.HealthValue;
import com.github.manolo8.darkbot.config.actions.values.HeroValue;
import com.github.manolo8.darkbot.config.actions.values.LocationConstant;
import com.github.manolo8.darkbot.config.actions.values.NumberConstant;
import com.github.manolo8.darkbot.config.actions.values.PercentConstant;
import com.github.manolo8.darkbot.config.actions.values.ShipLocationValue;
import com.github.manolo8.darkbot.config.actions.values.TargetValue;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ValueParser {
    private static final List<Class<? extends Value<?>>> AVAILABLE_VALUES =
            Arrays.asList(
                    AllCondition.class,
                    AnyCondition.class,
                    OneCondition.class,
                    NoneCondition.class,
                    NumericalCondition.class,
                    EqualCondition.class,
                    DistanceValue.class,
                    ShipLocationValue.class,
                    HealthTypeValue.class,
                    HealthValue.class,
                    HeroValue.class,
                    TargetValue.class,
                    NumberConstant.class,
                    PercentConstant.class,
                    BooleanConstant.class,
                    LocationConstant.class);

    private static final Map<String, ValueMeta> VALUES = buildMetadata();

    private static Map<String, ValueMeta> buildMetadata() {
        Map<String, ValueMeta> metadata = new HashMap<>();
        for (Class<? extends Value<?>> value : AVAILABLE_VALUES) {
            ValueMeta vm = new ValueMeta(value);
            metadata.put(vm.valueData.value(), vm);
        }
        return metadata;
    }

    public static Condition parseCondition(String str) throws SyntaxException {
        Result result = parse(str, Condition.Result.class);
        if (!result.leftover.trim().isEmpty())
            throw new SyntaxException("Unused characters after end", result.leftover);
        return (Condition) result.value;
    }

    public static Value<?> parseValue(String str) throws SyntaxException {
        Result result = parse(str);
        if (!result.leftover.trim().isEmpty())
            throw new SyntaxException("Unused characters after end", result.leftover);
        return result.value;
    }

    public static Result parse(String str) throws SyntaxException {
        return parse(str, Object.class);
    }

    public static Result parse(String str, Class<?> type) throws SyntaxException {
        String[] parts = str.trim().split(" *\\( *", 2);

        Stream<String> params = VALUES.values().stream()
                .filter(v -> type.isAssignableFrom(v.type)).map(v -> v.valueData.value());
        ValueMeta vm = VALUES.get(parts[0].trim());
        if (vm == null)
            throw new SyntaxException("Unknown value type '" + parts[0] + "'", str, params);
        if (!type.isAssignableFrom(vm.type))
            throw new SyntaxException("Invalid type, expected '" + type.getSimpleName() + "'", str, params);

        if (parts.length != 2)
            throw new SyntaxException("No start separator found", str, "(");

        str = parts[1].trim();

        Value<?> val = ReflectionUtils.createInstance(vm.clazz);
        if (val instanceof Parser) str = ((Parser) val).parse(str);
        else {
            for (Field field : vm.params) {
                Type[] types = ReflectionUtils.getTypes(field.getGenericType(), Value.class);
                if (types == null) types = ReflectionUtils.getTypes(field.getType(), Value.class);

                if (types == null || !(types[0] instanceof Class))
                    throw new SyntaxException("Error: Failed to find type info for " + field, str);

                Result pr = parse(str, (Class<?>) types[0]);

                if (!((Class<?>) types[0]).isAssignableFrom(pr.type))
                    throw new SyntaxException("Invalid parameter type, expected " + types[0].toString(), str);

                ReflectionUtils.set(field, val, pr.value);

                str = pr.leftover.trim();
                char expected = field == vm.params[vm.params.length - 1] ? ')' : ',';
                if (str.isEmpty() || str.charAt(0) != expected)
                    throw new SyntaxException("Missing separator in " + vm.valueData.value(), str, expected + "");
                str = str.substring(1);
            }
            if (vm.params.length == 0) { // No-param case
                if (str.isEmpty() || str.charAt(0) != ')')
                    throw new SyntaxException("Missing end separator in " + vm.valueData.value(), str, ")");
                str = str.substring(1);
            }
        }

        return new Result(val, vm.type, str);
    }

    public static class Result {
        public Value<?> value;
        public Class<?> type;
        public String leftover;

        public Result(Value<?> value, Class<?> type, String leftover) {
            this.value = value;
            this.type = type;
            this.leftover = leftover;
        }
    }

    private static class ValueMeta {
        private final ValueData valueData;
        private final Class<? extends Value<?>> clazz;
        private final Class<?> type;
        private final Field[] params;

        public ValueMeta(Class<? extends Value<?>> clazz) {
            this.valueData = clazz.getAnnotation(ValueData.class);
            if (valueData == null)
                throw new RuntimeException("Available value " + clazz.getName() + " has no value data!");
            this.clazz = clazz;
            Type[] t = ReflectionUtils.findGenericParameters(clazz, Value.class);
            if (t == null || !(t[0] instanceof Class))
                throw new RuntimeException("Value types must be findable classes. " + clazz.getName() + " has no class!");
            this.type = (Class<?>) t[0];
            this.params = clazz.getDeclaredFields();
        }
    }

}
