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
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

@ValueData(name = "if", description = "Compares two numbers", example = "if(a > b)")
public class NumericalCondition implements Condition, Parser {

    public Value<Number> a;
    public Operation operation;
    public Value<Number> b;

    @Override
    public @NotNull Condition.Result get(Main main) {
        Number numA, numB;
        if ((numA = Value.get(a, main)) == null || (numB = Value.get(b, main)) == null || operation == null)
            return Condition.Result.ABSTAIN;

        return Condition.Result.fromBoolean(operation.check.test(numA, numB));
    }

    public enum Operation {
        BIGGER(">", (a, b) -> a.doubleValue() > b.doubleValue()),
        BIGGER_OR_EQUAL(">=", (a, b) -> a.doubleValue() >= b.doubleValue()),
        EQUAL("=", (a, b) -> a.doubleValue() == b.doubleValue()),
        SMALLER_OR_EQUAL("<=", (a, b) -> a.doubleValue() <= b.doubleValue()),
        SMALLER("<", (a, b) -> a.doubleValue() < b.doubleValue());

        private final String display;
        private final BiPredicate<Number, Number> check;

        Operation(String display, BiPredicate<Number, Number> check) {
            this.display = display;
            this.check = check;
        }

        @Override
        public String toString() {
            return display;
        }

        public static Operation of(String operation) {
            for (Operation op : Operation.values()) {
                if (op.toString().equals(operation)) return op;
            }
            return null;
        }

    }

    @Override
    public String toString() {
        return "if(" + a + " " + operation + " " + b + ")";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        ParseResult<Number> prA = ValueParser.parse(str, Number.class);
        a = prA.value;
        str = prA.leftover.trim();

        int chars = Math.min(str.length(), str.length() > 1 && str.charAt(1) == '=' ? 2 : 1);

        String op = str.substring(0, chars);
        operation = Operation.of(op);
        if (operation == null)
            throw new SyntaxException("Unknown operation '" + op + "'", str, Operation.class);

        ParseResult<Number> prB = ValueParser.parse(str.substring(chars), Number.class);
        b = prB.value;
        return ParseUtil.separate(prB.leftover.trim(), getClass(), ")");
    }
}
