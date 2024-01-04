package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.config.actions.LegacyCondition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseResult;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import com.github.manolo8.darkbot.config.actions.tree.ParsingNode;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@ValueData(name = "equal", description = "Returns true if both parameters are the same", example = "equal(a, b)")
public class EqualCondition implements LegacyCondition, Parser {

    private Boolean isComparable;
    private Value<?> a, b;

    @Override
    public @NotNull Condition.Result get(PluginAPI api) {
        Object objA = Value.get(a, api), objB = Value.get(b, api);
        if (objA == null || objB == null) return Condition.Result.ABSTAIN;

        if (isComparable == null) isComparable = isComparable(objA, objB);

        if (isComparable) //noinspection unchecked
            return Condition.Result.fromBoolean(((Comparable<Object>) objA).compareTo(objB) == 0);
        return Condition.Result.fromBoolean(objA.equals(objB));
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
    public void parse(ParsingNode node) throws SyntaxException {
        node.requireParamSize(2, getClass());

        ParseResult<?> prA = ValueParser.parseGeneric(node.getParam(0));
        a = prA.value;
        b = ValueParser.parse(node.getParam(1), prA.type);

        isComparable = null;
    }

}
