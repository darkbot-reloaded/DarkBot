package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseResult;
import com.github.manolo8.darkbot.config.actions.parser.ParseUtil;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@ValueData(name = "equal", description = "Returns true if both parameters are the same", example = "equal(a, b)")
public class EqualCondition implements Condition, Parser {

    private Boolean isComparable;
    private Value<?> a, b;

    @Override
    public @NotNull Result get(Main main) {
        Object objA = Value.get(a, main), objB = Value.get(b, main);
        if (objA == null || objB == null) return Result.ABSTAIN;

        if (isComparable == null) isComparable = isComparable(objA, objB);

        if (isComparable) //noinspection unchecked
            return Result.fromBoolean(((Comparable<Object>) objA).compareTo(objB) == 0);
        return Result.fromBoolean(objA.equals(objB));
    }

    @Override
    public String toString() {
        return "equal(" + a + ", " + b + ")";
    }

    private boolean isComparable(Object a, Object b) {
        if (!(a instanceof Comparable)) return false;
        Type t = ReflectionUtils.findGenericParameters(a.getClass(), Comparable.class)[0];
        if (t instanceof Class) return ((Class<?>) t).isInstance(b);
        if (t instanceof ParameterizedType) {
            Type raw = ((ParameterizedType) t).getRawType();
            return raw instanceof Class && ((Class<?>) raw).isInstance(b);
        }
        return false;
    }

    @Override
    public String parse(String str) throws SyntaxException {
        ParseResult<?> prA = ValueParser.parse(str);
        a = prA.value;

        str = ParseUtil.separate(prA.leftover.trim(), getClass(), ",");

        ParseResult<?> prB = ValueParser.parse(str, prA.type);
        b = prB.value;

        isComparable = null;

        return ParseUtil.separate(prB.leftover.trim(), getClass(), ")");
    }

}
