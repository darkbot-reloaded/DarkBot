package com.github.manolo8.darkbot.config.actions.parser;

import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

public class ValueParser {

    @SuppressWarnings("deprecation")
    public static Condition parseCondition(String str) throws SyntaxException {
        ParseResult<Condition.Result> result = parse(str, Condition.Result.class);
        if (!result.leftover.trim().isEmpty())
            throw new SyntaxException("Unused characters after end", result.leftover);
        return (Condition) result.value;
    }

    public static Value<?> parseValue(String str) throws SyntaxException {
        ParseResult<?> result = parse(str);
        if (!result.leftover.trim().isEmpty())
            throw new SyntaxException("Unused characters after end", result.leftover);
        return result.value;
    }

    public static ParseResult<?> parse(String str) throws SyntaxException {
        return parse(str, Object.class);
    }

    public static <T> ParseResult<T> parse(String str, Class<T> type) throws SyntaxException {
        String[] parts = str.trim().split(" *\\( *", 2);

        Values.Meta<T> vm = Values.getMeta(parts[0].trim(), str, type);

        str = ParseUtil.separate(parts, vm, "(");

        Value<T> val = ReflectionUtils.createInstance(vm.clazz);
        if (val instanceof Parser) str = ((Parser) val).parse(str);
        else {
            for (Values.Param param : vm.params) {
                ParseResult<?> pr = parse(str, param.type);
                ReflectionUtils.set(param.field, val, pr.value);

                str = ParseUtil.separate(pr.leftover.trim(), vm, param == vm.params[vm.params.length - 1] ? ")" : ",");
            }

            if (vm.params.length == 0) str = ParseUtil.separate(str, vm, ")");
        }

        return new ParseResult<>(val, vm.type, str);
    }

}
