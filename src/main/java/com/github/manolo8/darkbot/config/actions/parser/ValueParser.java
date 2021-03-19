package com.github.manolo8.darkbot.config.actions.parser;

import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

public class ValueParser {

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

        if (parts.length != 2) throw new SyntaxException("No start separator found", "", vm, "(");
        str = parts[1].trim();


        Value<T> val = ReflectionUtils.createInstance(vm.clazz);
        if (val instanceof Parser) str = ((Parser) val).parse(str);
        else {
            for (Values.Param param : vm.params) {
                ParseResult<?> pr = parse(str, param.type);
                ReflectionUtils.set(param.field, val, pr.value);

                str = pr.leftover.trim();
                char expected = param == vm.params[vm.params.length - 1] ? ')' : ',';
                if (str.isEmpty() || str.charAt(0) != expected)
                    throw new SyntaxException("Missing separator in " + vm.getName(), str, vm, expected + "");
                str = str.substring(1);
            }

            if (vm.params.length == 0) { // No-param case
                if (str.isEmpty() || str.charAt(0) != ')')
                    throw new SyntaxException("Missing end separator in " + vm.getName(), str, vm, ")");
                str = str.substring(1);
            }
        }

        return new ParseResult<>(val, vm.type, str);
    }

}
